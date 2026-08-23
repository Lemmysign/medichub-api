package com.medichub.security;

import com.medichub.config.PaystackProperties;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class PaystackSignatureVerifierTest {

    // Arbitrary non-secret string used only as an HMAC key in these tests
    // (deliberately not in any provider's key format, to satisfy secret scanners).
    private static final String SECRET = "test-hmac-signing-key-0123456789abcdef";

    private PaystackSignatureVerifier verifier(String secret) {
        PaystackProperties props = new PaystackProperties(
                secret, "pk_test", "PLN_x", "https://api.paystack.co", "http://localhost/cb", 15000, 30000);
        return new PaystackSignatureVerifier(props);
    }

    private static String sign(String secret, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void validSignatureAccepted() throws Exception {
        String body = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"MHUB_1\"}}";
        String signature = sign(SECRET, body);

        assertThat(verifier(SECRET).isValid(body, signature)).isTrue();
    }

    @Test
    void tamperedBodyRejected() throws Exception {
        String body = "{\"event\":\"charge.success\",\"data\":{\"reference\":\"MHUB_1\"}}";
        String signature = sign(SECRET, body);
        String tampered = body.replace("MHUB_1", "MHUB_2");

        assertThat(verifier(SECRET).isValid(tampered, signature)).isFalse();
    }

    @Test
    void wrongSecretRejected() throws Exception {
        String body = "{\"event\":\"charge.success\"}";
        String signature = sign("some_other_secret", body);

        assertThat(verifier(SECRET).isValid(body, signature)).isFalse();
    }

    @Test
    void blankOrNullSignatureRejected() {
        assertThat(verifier(SECRET).isValid("{}", null)).isFalse();
        assertThat(verifier(SECRET).isValid("{}", "")).isFalse();
    }

    @Test
    void unconfiguredSecretRejects() throws Exception {
        String body = "{}";
        String signature = sign(SECRET, body);

        assertThat(verifier("").isValid(body, signature)).isFalse();
    }
}
