package com.medichub.exception;

/** Thrown for invalid client input or violated business preconditions. Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
