package com.medichub.service.impl;

import com.medichub.dto.request.ForgotPasswordRequest;
import com.medichub.dto.request.LoginRequest;
import com.medichub.dto.request.RegisterRequest;
import com.medichub.dto.request.ResendOtpRequest;
import com.medichub.dto.request.ResetPasswordRequest;
import com.medichub.dto.request.VerifyOtpRequest;
import com.medichub.dto.response.AuthResponse;
import com.medichub.dto.response.OtpChallengeResponse;
import com.medichub.dto.response.VerifyOtpResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.EmailNotVerifiedException;
import com.medichub.exception.InstructorNotApprovedException;
import com.medichub.mapper.UserMapper;
import com.medichub.model.EmailVerificationToken;
import com.medichub.model.PasswordResetToken;
import com.medichub.model.RefreshToken;
import com.medichub.model.User;
import com.medichub.model.enums.Role;
import com.medichub.repository.EmailVerificationTokenRepository;
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
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
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
                           EmailVerificationTokenRepository emailVerificationTokenRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider tokenProvider,
                           AuthenticationManager authenticationManager,
                           EmailService emailService,
                           UserMapper userMapper,
                           @Value("${app.admin.email}") String adminEmail) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.userMapper = userMapper;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
    }

    @Override
    public OtpChallengeResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        Role role = resolveRole(email, request.role());
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        user.setEnabled(true);
        user.setEmailVerified(false);
        // Instructors require admin approval; students and the admin are auto-approved.
        user.setApproved(role != Role.INSTRUCTOR);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // Lost the race against a concurrent signup with the same email.
            throw new BadRequestException("An account with this email already exists");
        }

        issueOtp(user);
        return new OtpChallengeResponse(email, "We've sent a 6-digit verification code to your email.");
    }

    // noRollbackFor: a wrong/expired code must still persist the burned attempt (rolling back
    // would reset the attempt counter and defeat the cap).
    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid or expired code"));

        if (user.isEmailVerified()) {
            // Already verified — treat as success and route by approval state.
            return user.getRole() == Role.INSTRUCTOR && !user.isApproved()
                    ? VerifyOtpResponse.pending()
                    : VerifyOtpResponse.loggedIn(issueTokens(user));
        }

        EmailVerificationToken token = emailVerificationTokenRepository
                .findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("No active code — please request a new one"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setUsed(true);
            throw new BadRequestException("This code has expired — please request a new one");
        }
        if (token.getAttempts() >= OTP_MAX_ATTEMPTS) {
            token.setUsed(true);
            throw new BadRequestException("Too many attempts — please request a new code");
        }
        if (!passwordEncoder.matches(request.code(), token.getCodeHash())) {
            token.setAttempts(token.getAttempts() + 1);
            throw new BadRequestException("Incorrect code");
        }

        token.setUsed(true);
        user.setEmailVerified(true);

        if (user.getRole() == Role.INSTRUCTOR && !user.isApproved()) {
            log.info("Instructor userId={} verified email; awaiting admin approval", user.getId());
            return VerifyOtpResponse.pending();
        }
        return VerifyOtpResponse.loggedIn(issueTokens(user));
    }

    @Override
    public OtpChallengeResponse resendOtp(ResendOtpRequest request) {
        String email = normalizeEmail(request.email());
        // Never reveal whether the email exists or is already verified.
        userRepository.findByEmail(email)
                .filter(u -> !u.isEmailVerified())
                .ifPresent(this::issueOtp);
        return new OtpChallengeResponse(email, "If that account needs verifying, a new code is on its way.");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        // Delegates credential + enabled checks to the AuthenticationManager;
        // BadCredentialsException / DisabledException are mapped by the global handler.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Post-credential gates (only reachable with a correct password, so no account-enumeration).
        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email to continue");
        }
        if (user.getRole() == Role.INSTRUCTOR && !user.isApproved()) {
            throw new InstructorNotApprovedException("Your instructor account is awaiting admin approval");
        }
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

    /** Burns any outstanding code, generates a fresh 6-digit OTP, stores its hash, and emails it. */
    private void issueOtp(User user) {
        emailVerificationTokenRepository.invalidateAllForUser(user);

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setCodeHash(passwordEncoder.encode(code));
        token.setExpiresAt(Instant.now().plus(OTP_TTL));
        token.setUsed(false);
        token.setAttempts(0);
        emailVerificationTokenRepository.save(token);

        emailService.sendOtpEmail(user.getEmail(), code);
    }

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
