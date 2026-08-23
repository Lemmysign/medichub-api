package com.medichub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Paystack settings (CLAUDE.md §7). Bound from {@code app.paystack.*}. */
@ConfigurationProperties(prefix = "app.paystack")
public record PaystackProperties(
        String secretKey,
        String publicKey,
        String planCode,
        String baseUrl,
        String callbackUrl,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
