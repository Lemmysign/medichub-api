package com.medichub.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * TODO(Phase: Google OAuth2) — success handler for Google login.
 *
 * <p>This is a deliberate stub. Google OAuth2 is NOT wired into the security filter
 * chain yet because it needs real Google client credentials to test. When enabled
 * (via {@code http.oauth2Login(...)} in SecurityConfig), Google sign-in must funnel
 * into the SAME JWT issuance as local login so the rest of the app treats every user
 * identically (CLAUDE.md §5.1). The intended flow:
 *
 * <ol>
 *   <li>Extract email + name + provider id from the {@code OAuth2User}.</li>
 *   <li>Find the {@link com.medichub.model.User} by email, or provision a new one
 *       with {@code authProvider = GOOGLE} (delegated to AuthService, not done here).</li>
 *   <li>Mint the access + refresh JWT pair via {@link JwtTokenProvider} and persist
 *       the refresh token (same as local login).</li>
 *   <li>Redirect back to {@code app.frontend.url} with the tokens.</li>
 * </ol>
 *
 * Kept as a bean so it is ready to inject once the flow is implemented.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(JwtTokenProvider tokenProvider,
                                     @Value("${app.frontend.url}") String frontendUrl) {
        this.tokenProvider = tokenProvider;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // TODO(Phase: Google OAuth2): provision/find user, mint JWTs via tokenProvider,
        // persist refresh token, then redirect to the SPA with the token pair.
        response.sendRedirect(frontendUrl + "/login?error=google_oauth_not_implemented");
    }
}
