package com.medichub.dto.response;

import java.time.Instant;

/** Material metadata; the file itself is fetched via a separate short-lived signed URL. */
public record MaterialResponse(
        Long id,
        String fileName,
        String contentType,
        Long sizeBytes,
        Long topicId,
        Instant createdAt
) {
}
