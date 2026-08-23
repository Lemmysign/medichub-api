package com.medichub.controller;

import com.medichub.dto.paystack.PaystackDTO;
import com.medichub.security.PaystackSignatureVerifier;
import com.medichub.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

/**
 * Paystack webhook (CLAUDE.md §3, §6). Public (no JWT) but the HMAC-SHA512
 * signature is verified against the raw body BEFORE any processing. Never trusts
 * an unverified payload.
 */
@RestController
@RequestMapping("/api/webhooks/paystack")
public class PaystackWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaystackWebhookController.class);

    private final PaystackSignatureVerifier signatureVerifier;
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper;

    public PaystackWebhookController(PaystackSignatureVerifier signatureVerifier,
                                    SubscriptionService subscriptionService,
                                    ObjectMapper objectMapper) {
        this.signatureVerifier = signatureVerifier;
        this.subscriptionService = subscriptionService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<String> handle(
            @RequestBody(required = false) String rawBody,
            @RequestHeader(value = "x-paystack-signature", required = false) String signature) {

        if (rawBody == null || rawBody.isBlank()) {
            return ResponseEntity.badRequest().body("Empty body");
        }
        if (!signatureVerifier.isValid(rawBody, signature)) {
            log.warn("Rejected Paystack webhook with invalid/missing signature");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }

        PaystackDTO.WebhookEvent event;
        try {
            event = objectMapper.readValue(rawBody, PaystackDTO.WebhookEvent.class);
        } catch (Exception e) {
            log.warn("Malformed Paystack webhook payload: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid payload");
        }

        try {
            subscriptionService.processWebhookEvent(event);
        } catch (Exception e) {
            // Signature was valid; log and still 200 so Paystack does not hammer retries
            // for a transient internal error. Reconciliation via /verify remains available.
            log.error("Error processing Paystack webhook: {}", e.getMessage(), e);
        }
        return ResponseEntity.ok("Webhook received");
    }
}
