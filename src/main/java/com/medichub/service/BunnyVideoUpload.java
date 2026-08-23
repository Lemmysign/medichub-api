package com.medichub.service;

/**
 * Browser-direct upload credential for a freshly-created Bunny video (CLAUDE.md §6).
 * The SPA uploads via Bunny's TUS endpoint using these values as headers
 * (AuthorizationSignature, AuthorizationExpire, LibraryId, VideoId) — the API key
 * never reaches the browser.
 */
public record BunnyVideoUpload(
        String guid,
        String libraryId,
        String tusEndpoint,
        long expiresAtEpochSeconds,
        String signature
) {
}
