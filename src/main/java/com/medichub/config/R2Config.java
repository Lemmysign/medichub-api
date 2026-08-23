package com.medichub.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Cloudflare R2 clients via the AWS S3 SDK (CLAUDE.md §6). Bean construction does
 * no network I/O, so the app starts even with placeholder credentials; the first
 * real call is what needs valid keys.
 */
@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2Config {

    private final R2Properties props;

    public R2Config(R2Properties props) {
        this.props = props;
    }

    private StaticCredentialsProvider credentials() {
        // The AWS SDK rejects blank credentials at build time, so fall back to a
        // clearly-fake placeholder when R2 keys aren't configured. This lets the app
        // start credential-free (local dev / CI); only a real R2 call needs valid keys.
        String accessKey = hasText(props.accessKey()) ? props.accessKey() : "unconfigured-access-key";
        String secretKey = hasText(props.secretKey()) ? props.secretKey() : "unconfigured-secret-key";
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Bean
    public S3Client r2S3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(credentials())
                // R2 works most reliably with path-style access.
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Bean
    public S3Presigner r2S3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(props.endpoint()))
                .region(Region.of("auto"))
                .credentialsProvider(credentials())
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }
}
