package com.medichub.service.impl;

import com.medichub.dto.response.CourseProgressResponse;
import com.medichub.dto.response.EnrolledCourseResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.model.Course;
import com.medichub.model.Enrollment;
import com.medichub.repository.CourseRepository;
import com.medichub.repository.EnrollmentRepository;
import com.medichub.repository.TopicProgressRepository;
import com.medichub.repository.TopicRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CourseService;
import com.medichub.service.EnrollmentService;
import com.medichub.service.ProgressCalculator;
import com.medichub.service.SubscriptionAccessService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final TopicProgressRepository topicProgressRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final SubscriptionAccessService subscriptionAccessService;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 CourseRepository courseRepository,
                                 TopicRepository topicRepository,
                                 TopicProgressRepository topicProgressRepository,
                                 UserRepository userRepository,
                                 CourseService courseService,
                                 SubscriptionAccessService subscriptionAccessService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.topicProgressRepository = topicProgressRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Override
    public CourseProgressResponse openCourse(Long courseId) {
        Long studentId = SecurityUtils.currentUserId();
        courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(studentId);

        upsertEnrollment(studentId, courseId);

        long total = topicRepository.countByCourseId(courseId);
        long completed = topicProgressRepository.countCompletedByStudentAndCourse(studentId, courseId);
        return new CourseProgressResponse(courseId, total, completed, ProgressCalculator.percent(completed, total));
    }

    @Override
    public void ensureEnrolled(Long courseId) {
        upsertEnrollment(SecurityUtils.currentUserId(), courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EnrolledCourseResponse> listMyCourses(Pageable pageable) {
        Long studentId = SecurityUtils.currentUserId();
        Page<Enrollment> page = enrollmentRepository.findByStudentId(studentId, pageable);

        List<Long> courseIds = page.getContent().stream().map(e -> e.getCourse().getId()).toList();
        Map<Long, Long> totals = new HashMap<>();
        Map<Long, Long> completed = new HashMap<>();
        if (!courseIds.isEmpty()) {
            for (Object[] row : courseRepository.countTopicsByCourseIds(courseIds)) {
                totals.put((Long) row[0], (Long) row[1]);
            }
            for (Object[] row : topicProgressRepository.countCompletedByStudentAndCourses(studentId, courseIds)) {
                completed.put((Long) row[0], (Long) row[1]);
            }
        }

        return PagedResponse.from(page, enrollment -> {
            Course course = enrollment.getCourse();
            long total = totals.getOrDefault(course.getId(), 0L);
            long done = completed.getOrDefault(course.getId(), 0L);
            return new EnrolledCourseResponse(
                    course.getId(),
                    course.getTitle(),
                    course.getThumbnailUrl(),
                    course.getInstructor().getFullName(),
                    total,
                    done,
                    ProgressCalculator.percent(done, total),
                    enrollment.getEnrolledAt(),
                    enrollment.getLastAccessedAt());
        });
    }

    /** Idempotent enroll + touch lastAccessedAt; tolerant of a concurrent first-open race. */
    private void upsertEnrollment(Long studentId, Long courseId) {
        Enrollment existing = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).orElse(null);
        Instant now = Instant.now();
        if (existing != null) {
            existing.setLastAccessedAt(now);
            return;
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(userRepository.getReferenceById(studentId));
        userRepository.findById(studentId).ifPresent(u -> enrollment.setStudentName(u.getFullName()));
        enrollment.setCourse(courseRepository.getReferenceById(courseId));
        enrollment.setEnrolledAt(now);
        enrollment.setLastAccessedAt(now);
        try {
            enrollmentRepository.save(enrollment);
        } catch (DataIntegrityViolationException race) {
            // Concurrent first-open created it; treat as success.
            enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                    .ifPresent(e -> e.setLastAccessedAt(now));
        }
    }
}
