package com.medichub.service;

import com.medichub.config.PaystackProperties;
import com.medichub.dto.paystack.PaystackDTO;
import com.medichub.exception.PaymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Low-level Paystack REST calls (adapted from the Cafeteria backend). No DB, no
 * transactions — just the HTTP integration.
 */
@Component
public class PaystackClient {

    private static final Logger log = LoggerFactory.getLogger(PaystackClient.class);

    private final RestTemplate paystackRestTemplate;
    private final PaystackProperties props;

    public PaystackClient(RestTemplate paystackRestTemplate, PaystackProperties props) {
        this.paystackRestTemplate = paystackRestTemplate;
        this.props = props;
    }

    public PaystackDTO.InitializeResponse initialize(String reference, long amountKobo, String email, String currency) {
        if (props.secretKey() == null || props.secretKey().isBlank()) {
            throw new PaymentException("Online payments are not set up yet. Please try again later.");
        }
        PaystackDTO.InitializeRequest request = new PaystackDTO.InitializeRequest();
        request.setAmount(amountKobo);
        request.setEmail(email);
        request.setReference(reference);
        request.setCurrency(currency);
        request.setCallbackUrl(props.callbackUrl());

        try {
            String url = props.baseUrl() + "/transaction/initialize";
            ResponseEntity<PaystackDTO.InitializeResponse> response = paystackRestTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(request), PaystackDTO.InitializeResponse.class);
            return response.getBody();
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to initialize Paystack transaction for reference {}: {}", reference, e.getMessage(), e);
            throw new PaymentException("Could not start payment right now. Please try again later.");
        }
    }

    public PaystackDTO.VerifyResponse verify(String reference) {
        try {
            String url = props.baseUrl() + "/transaction/verify/" + reference;
            ResponseEntity<PaystackDTO.VerifyResponse> response = paystackRestTemplate.exchange(
                    url, HttpMethod.GET, HttpEntity.EMPTY, PaystackDTO.VerifyResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to verify Paystack transaction for reference {}: {}", reference, e.getMessage(), e);
            return null;
        }
    }
}
