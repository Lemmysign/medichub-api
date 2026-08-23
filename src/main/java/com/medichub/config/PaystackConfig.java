package com.medichub.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Paystack HTTP client (adapted from the Cafeteria backend's PaystackConfig):
 * a RestTemplate whose interceptor injects the secret key as a Bearer token and
 * forces JSON. Building the client does no network I/O, so the app starts without
 * real keys; live calls need a valid secret.
 */
@Configuration
@EnableConfigurationProperties({PaystackProperties.class, SubscriptionPlanProperties.class})
public class PaystackConfig {

    @Bean
    public RestTemplate paystackRestTemplate(PaystackProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeoutMs());
        factory.setReadTimeout(props.readTimeoutMs());

        String secretKey = props.secretKey() == null ? "" : props.secretKey();

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + secretKey);
            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
