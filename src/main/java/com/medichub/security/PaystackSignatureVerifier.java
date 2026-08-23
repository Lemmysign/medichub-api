package com.medichub.security;

import com.medichub.config.PaystackProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Verifies the Paystack webhook signature: HMAC-SHA512 of the raw request body
 * using the secret key, compared constant-time against the {@code x-paystack-signature}
 * header (adapted from the Cafeteria backend's PaymentController#verifySignature).
 */
@Component
public class PaystackSignatureVerifier {

    private static final Logger log = LoggerFactory.getLogger(PaystackSignatureVerifier.class);
    private static final String HMAC_SHA512 = "HmacSHA512";

    private final PaystackProperties props;

    public PaystackSignatureVerifier(PaystackProperties props) {
        this.props = props;
    }

    public boolean isValid(String rawBody, String receivedSignature) {
        if (rawBody == null || receivedSignature == null || receivedSignature.isBlank()) {
            return false;
        }
        String secret = props.secretKey();
        if (secret == null || secret.isBlank()) {
            log.error("Paystack secret key not configured; rejecting webhook");
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA512));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String computed = HexFormat.of().formatHex(hash);
            return MessageDigest.isEqual(
                    computed.getBytes(StandardCharsets.UTF_8),
                    receivedSignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Paystack signature verification error: {}", e.getMessage(), e);
            return false;
        }
    }
}
