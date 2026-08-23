package com.medichub.dto.response;

/**
 * Returned to the instructor to drive a browser-direct upload to Bunny (TUS). The
 * SPA sends these as TUS headers; the Bunny API key never reaches the browser.
 */
public record VideoUploadCredentialResponse(
        Long topicId,
        String bunnyVideoId,
        String libraryId,
        String tusEndpoint,
        long expiresAtEpochSeconds,
        String signature
) {
}
