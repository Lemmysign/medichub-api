package com.medichub.security;

import com.medichub.model.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the Bearer access token, verifies it, and populates the SecurityContext
 * straight from the claims — deliberately no DB lookup, keeping request handling
 * stateless and cheap. Trade-off: a disabled account keeps access until its access
 * token expires (access TTL ~30 min); refresh is where revocation bites.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final DisabledUserRegistry disabledUserRegistry;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, DisabledUserRegistry disabledUserRegistry) {
        this.tokenProvider = tokenProvider;
        this.disabledUserRegistry = disabledUserRegistry;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(token, request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            Claims claims = tokenProvider.parse(token);
            // Only access tokens authenticate a request; refresh tokens are for /refresh only.
            if (!JwtTokenProvider.TOKEN_TYPE_ACCESS.equals(tokenProvider.getType(claims))) {
                return;
            }
            Long userId = tokenProvider.getUserId(claims);
            // Reject immediately if the account has been disabled since the token was issued.
            if (disabledUserRegistry.isDisabled(userId)) {
                SecurityContextHolder.clearContext();
                return;
            }
            Role role = Role.valueOf(tokenProvider.getRole(claims));
            CustomUserDetails principal = new CustomUserDetails(
                    userId,
                    tokenProvider.getEmail(claims),
                    null,
                    role,
                    true);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException ex) {
            // Invalid/expired token → leave the context unauthenticated; the entry
            // point will return 401 if the endpoint requires authentication.
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (StringUtils.hasText(header) && header.startsWith(PREFIX)) {
            return header.substring(PREFIX.length());
        }
        return null;
    }
}
