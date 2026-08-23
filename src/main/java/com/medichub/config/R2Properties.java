package com.medichub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Cloudflare R2 settings (CLAUDE.md §7). Bound from {@code app.r2.*}. */
@ConfigurationProperties(prefix = "app.r2")
public record R2Properties(
        String accountId,
        String accessKey,
        String secretKey,
        String bucket,
        String publicBaseUrl
) {
    public String endpoint() {
        return "https://" + accountId + ".r2.cloudflarestorage.com";
    }
}
