package com.medichub.dto.response;

public record TestResponse(
        Long id,
        Long courseId,
        String title,
        int passMarkPercent,
        long questionCount
) {
}
