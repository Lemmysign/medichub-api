package com.medichub.dto.response;

import com.medichub.model.enums.Role;

import java.time.Instant;
import java.util.List;

/**
 * A Q&A thread node. For a root question, {@code replies} carries the answers and
 * {@code answered} indicates whether an instructor has replied. For a reply node,
 * {@code replies} is empty and {@code parentId} points at the question.
 */
public record CommentResponse(
        Long id,
        String text,
        Long authorId,
        String authorName,
        Role authorRole,
        Long courseId,
        String courseTitle,
        Long topicId,
        String topicTitle,
        Long parentId,
        boolean answered,
        Instant createdAt,
        List<CommentResponse> replies
) {
}
