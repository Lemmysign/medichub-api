package com.medichub.dto.response;

/** Public/preview topic view — title & ordering only, never playback data. */
public record TopicPreviewResponse(
        Long id,
        String title,
        int orderIndex,
        Integer videoDurationSeconds,
        boolean hasVideo
) {
}
