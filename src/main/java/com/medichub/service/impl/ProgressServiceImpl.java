package com.medichub.service.impl;

import com.medichub.dto.request.MarkProgressRequest;
import com.medichub.dto.response.CourseProgressResponse;
import com.medichub.dto.response.TopicProgressResponse;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.model.Topic;
import com.medichub.model.TopicProgress;
import com.medichub.repository.TopicProgressRepository;
import com.medichub.repository.TopicRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CourseService;
import com.medichub.service.EnrollmentService;
import com.medichub.service.ProgressCalculator;
import com.medichub.service.ProgressService;
import com.medichub.service.SubscriptionAccessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class ProgressServiceImpl implements ProgressService {

    private final TopicProgressRepository topicProgressRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final SubscriptionAccessService subscriptionAccessService;
    private final EnrollmentService enrollmentService;
    private final int completeThresholdSeconds;

    public ProgressServiceImpl(TopicProgressRepository topicProgressRepository,
                               TopicRepository topicRepository,
                               UserRepository userRepository,
                               CourseService courseService,
                               SubscriptionAccessService subscriptionAccessService,
                               EnrollmentService enrollmentService,
                               @Value("${app.progress.complete-threshold-seconds}") int completeThresholdSeconds) {
        this.topicProgressRepository = topicProgressRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.subscriptionAccessService = subscriptionAccessService;
        this.enrollmentService = enrollmentService;
        this.completeThresholdSeconds = completeThresholdSeconds;
    }

    @Override
    public TopicProgressResponse markTopicWatched(Long courseId, Long topicId, MarkProgressRequest request) {
        Long studentId = SecurityUtils.currentUserId();
        courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(studentId);

        Topic topic = topicRepository.findByIdAndCourseId(topicId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", topicId));
        enrollmentService.ensureEnrolled(courseId);

        TopicProgress progress = topicProgressRepository.findByStudentIdAndTopicId(studentId, topicId)
                .orElseGet(() -> {
                    TopicProgress created = new TopicProgress();
                    created.setStudent(userRepository.getReferenceById(studentId));
                    created.setTopic(topic);
                    created.setSecondsWatched(0);
                    created.setCompleted(false);
                    return created;
                });

        // Track the furthest point watched; never let a smaller report regress it.
        progress.setSecondsWatched(Math.max(progress.getSecondsWatched(), request.secondsWatched()));
        if (!progress.isCompleted() && progress.getSecondsWatched() >= completeThresholdSeconds) {
            progress.setCompleted(true);
            progress.setCompletedAt(Instant.now());
        }
        progress = topicProgressRepository.save(progress);

        return new TopicProgressResponse(
                topicId, progress.isCompleted(), progress.getSecondsWatched(), progress.getCompletedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseProgressResponse getCourseProgress(Long courseId) {
        Long studentId = SecurityUtils.currentUserId();
        courseService.requirePublishedCourse(courseId);
        long total = topicRepository.countByCourseId(courseId);
        long completed = topicProgressRepository.countCompletedByStudentAndCourse(studentId, courseId);
        return new CourseProgressResponse(courseId, total, completed, ProgressCalculator.percent(completed, total));
    }
}
