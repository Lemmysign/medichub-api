package com.medichub.dto.response;

import java.time.Instant;

public record TopicProgressResponse(
        Long topicId,
        boolean completed,
        int secondsWatched,
        Instant completedAt
) {
}
