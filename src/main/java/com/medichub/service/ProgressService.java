package com.medichub.service;

import com.medichub.dto.request.MarkProgressRequest;
import com.medichub.dto.response.CourseProgressResponse;
import com.medichub.dto.response.TopicProgressResponse;

public interface ProgressService {

    /** Record watch seconds for a topic; marks complete once the threshold is reached. Gated. */
    TopicProgressResponse markTopicWatched(Long courseId, Long topicId, MarkProgressRequest request);

    /** Current student's progress for a course (completed / total). */
    CourseProgressResponse getCourseProgress(Long courseId);
}
