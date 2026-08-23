package com.medichub.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Bunny Stream HTTP client (CLAUDE.md §6). Building the client does no I/O, so the
 * app starts without a real API key; calls made against Bunny need valid config.
 */
@Configuration
@EnableConfigurationProperties(BunnyProperties.class)
public class BunnyConfig {

    @Bean
    public RestClient bunnyRestClient(BunnyProperties props) {
        return RestClient.builder()
                .baseUrl("https://video.bunnycdn.com")
                .defaultHeader("AccessKey", props.apiKey() == null ? "" : props.apiKey())
                .defaultHeader("Accept", "application/json")
                .build();
    }
}
