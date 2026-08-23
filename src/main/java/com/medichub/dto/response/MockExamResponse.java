package com.medichub.dto.response;

/** Creator (instructor/admin) view of a mock exam. */
public record MockExamResponse(
        Long id,
        String title,
        String description,
        int passMarkPercent,
        Integer durationMinutes,
        boolean published,
        String ownerName,
        long questionCount
) {
}
