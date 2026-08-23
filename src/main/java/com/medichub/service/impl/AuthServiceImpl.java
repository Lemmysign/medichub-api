package com.medichub.service.impl;

import com.medichub.dto.request.ForgotPasswordRequest;
import com.medichub.dto.request.LoginRequest;
import com.medichub.dto.request.RegisterRequest;
import com.medichub.dto.request.ResetPasswordRequest;
import com.medichub.dto.response.AuthResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.mapper.UserMapper;
import com.medichub.model.PasswordResetToken;
import com.medichub.model.RefreshToken;
import com.medichub.model.User;
import com.medichub.model.enums.AuthProvider;
import com.medichub.model.enums.Role;
import com.medichub.repository.PasswordResetTokenRepository;
import com.medichub.repository.RefreshTokenRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.JwtTokenProvider;
import com.medichub.service.AuthService;
import com.medichub.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final String adminEmail;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider,
                           AuthenticationManager authenticationManager,
                           EmailService emailService,
                           UserMapper userMapper,
                           @Value("${app.admin.email}") String adminEmail) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.userMapper = userMapper;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(resolveRole(email, request.role()));
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEnabled(true);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Lost the race against a concurrent signup with the same email.
            throw new BadRequestException("An account with this email already exists");
        }
        return issueTokens(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Delegates credential + enabled checks to the AuthenticationManager;
        // BadCredentialsException / DisabledException are mapped by the global handler.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        return issueTokens(user);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!tokenProvider.isValid(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }
        var claims = tokenProvider.parse(refreshToken);
        if (!JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(tokenProvider.getType(claims))) {
            throw new BadRequestException("Invalid refresh token");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        // Rotation: retire the presented token so it cannot be replayed.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(stored.getUser());
    }

    @Override
    public void logout(String refreshToken) {
        // Idempotent: silently succeed if the token is unknown or already revoked.
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        // Do not reveal whether the email exists — always behave the same to the caller.
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.invalidateAllForUser(user);

            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setToken(generateSecureToken());
            prt.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL));
            prt.setUsed(false);
            passwordResetTokenRepository.save(prt);

            emailService.sendPasswordResetEmail(user.getEmail(), prt.getToken());
            log.info("Password reset requested for userId={}", user.getId());
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (prt.isUsed() || prt.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);

        // Force re-login everywhere after a password change.
        refreshTokenRepository.revokeAllForUser(user);
    }

    // ----------------------------------------------------------------------

    private AuthResponse issueTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        RefreshToken stored = new RefreshToken();
        stored.setUser(user);
        stored.setToken(refreshToken);
        stored.setExpiresAt(tokenProvider.getRefreshExpiry());
        stored.setRevoked(false);
        refreshTokenRepository.save(stored);

        return AuthResponse.of(accessToken, refreshToken, tokenProvider.getAccessTtlSeconds(),
                userMapper.toResponse(user));
    }

    /** ADMIN only for the preconfigured admin email; ADMIN can never be self-selected. */
    private Role resolveRole(String email, Role requestedRole) {
        if (!adminEmail.isEmpty() && email.equalsIgnoreCase(adminEmail)) {
            return Role.ADMIN;
        }
        if (requestedRole == Role.ADMIN) {
            throw new BadRequestException("Cannot register as admin");
        }
        return requestedRole;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
