package com.medichub.dto.response;

/** Full topic view for the instructor (includes the Bunny GUID). */
public record TopicResponse(
        Long id,
        String title,
        int orderIndex,
        String bunnyVideoId,
        Integer videoDurationSeconds,
        boolean hasVideo
) {
}
