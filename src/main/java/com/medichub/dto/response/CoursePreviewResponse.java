package com.medichub.dto.response;

import java.util.List;

/** Public course detail: metadata + ordered topic titles, no playback data. */
public record CoursePreviewResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        boolean published,
        Long instructorId,
        String instructorName,
        long topicCount,
        List<TopicPreviewResponse> topics
) {
}
