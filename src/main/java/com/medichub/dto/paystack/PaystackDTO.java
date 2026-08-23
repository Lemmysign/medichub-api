package com.medichub.dto.paystack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Wire DTOs for the Paystack REST API (adapted from the Cafeteria backend).
 * These map Paystack's JSON, distinct from our own request/response DTOs.
 */
public final class PaystackDTO {

    private PaystackDTO() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitializeRequest {
        private Long amount;            // in kobo
        private String email;
        private String reference;
        @JsonProperty("callback_url")
        private String callbackUrl;
        private String currency;

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getCallbackUrl() {
            return callbackUrl;
        }

        public void setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InitializeResponse {
        private boolean status;
        private String message;
        private Data data;

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            @JsonProperty("authorization_url")
            private String authorizationUrl;
            @JsonProperty("access_code")
            private String accessCode;
            private String reference;

            public String getAuthorizationUrl() {
                return authorizationUrl;
            }

            public void setAuthorizationUrl(String authorizationUrl) {
                this.authorizationUrl = authorizationUrl;
            }

            public String getAccessCode() {
                return accessCode;
            }

            public void setAccessCode(String accessCode) {
                this.accessCode = accessCode;
            }

            public String getReference() {
                return reference;
            }

            public void setReference(String reference) {
                this.reference = reference;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VerifyResponse {
        private boolean status;
        private String message;
        private Data data;

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            private String status;          // "success", "failed", ...
            private String reference;
            private BigDecimal amount;      // in kobo
            @JsonProperty("paid_at")
            private String paidAt;
            private String currency;
            @JsonProperty("customer")
            private Customer customer;

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getReference() {
                return reference;
            }

            public void setReference(String reference) {
                this.reference = reference;
            }

            public BigDecimal getAmount() {
                return amount;
            }

            public void setAmount(BigDecimal amount) {
                this.amount = amount;
            }

            public String getPaidAt() {
                return paidAt;
            }

            public void setPaidAt(String paidAt) {
                this.paidAt = paidAt;
            }

            public String getCurrency() {
                return currency;
            }

            public void setCurrency(String currency) {
                this.currency = currency;
            }

            public Customer getCustomer() {
                return customer;
            }

            public void setCustomer(Customer customer) {
                this.customer = customer;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Customer {
            private String email;
            @JsonProperty("customer_code")
            private String customerCode;

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public String getCustomerCode() {
                return customerCode;
            }

            public void setCustomerCode(String customerCode) {
                this.customerCode = customerCode;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookEvent {
        private String event;
        private Data data;

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Data {
            private String reference;
            private BigDecimal amount;
            private String status;

            public String getReference() {
                return reference;
            }

            public void setReference(String reference) {
                this.reference = reference;
            }

            public BigDecimal getAmount() {
                return amount;
            }

            public void setAmount(BigDecimal amount) {
                this.amount = amount;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }
        }
    }
}
