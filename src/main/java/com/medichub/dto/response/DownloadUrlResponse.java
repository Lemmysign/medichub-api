package com.medichub.dto.response;

/** A short-lived signed URL for downloading a gated resource. */
public record DownloadUrlResponse(
        String url,
        long expiresInSeconds
) {
}
