package com.medichub.dto.response;

import java.time.Instant;

/** A study document as shown in creator and student lists. */
public record StudyMaterialResponse(
        Long id,
        String title,
        String description,
        Long subjectId,
        String subjectName,
        String fileName,
        String contentType,
        Long sizeBytes,
        boolean published,
        String ownerName,
        Instant createdAt
) {
}
