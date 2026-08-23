package com.medichub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Bunny Stream settings (CLAUDE.md §7). Bound from {@code app.bunny.*}. */
@ConfigurationProperties(prefix = "app.bunny")
public record BunnyProperties(
        String libraryId,
        String apiKey,
        String cdnHostname,
        String tokenAuthKey
) {
}
