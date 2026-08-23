package com.medichub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Enables {@code @Async} so side effects like sending email run off the request thread. */
@Configuration
@EnableAsync
public class AsyncConfig {
}
