package com.medichub.controller;

import com.medichub.dto.request.MarkProgressRequest;
import com.medichub.dto.response.CourseProgressResponse;
import com.medichub.dto.response.EnrolledCourseResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.TopicProgressResponse;
import com.medichub.service.EnrollmentService;
import com.medichub.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Student enrollment & progress (subscription-gated in the service layer). */
@RestController
@RequestMapping("/api/student/courses")
public class StudentLearningController {

    private final EnrollmentService enrollmentService;
    private final ProgressService progressService;

    public StudentLearningController(EnrollmentService enrollmentService, ProgressService progressService) {
        this.enrollmentService = enrollmentService;
        this.progressService = progressService;
    }

    @GetMapping
    public PagedResponse<EnrolledCourseResponse> myCourses(@PageableDefault(size = 20) Pageable pageable) {
        return enrollmentService.listMyCourses(pageable);
    }

    @PostMapping("/{courseId}/open")
    public CourseProgressResponse open(@PathVariable Long courseId) {
        return enrollmentService.openCourse(courseId);
    }

    @GetMapping("/{courseId}/progress")
    public CourseProgressResponse progress(@PathVariable Long courseId) {
        return progressService.getCourseProgress(courseId);
    }

    @PostMapping("/{courseId}/topics/{topicId}/progress")
    public TopicProgressResponse markWatched(@PathVariable Long courseId,
                                             @PathVariable Long topicId,
                                             @Valid @RequestBody MarkProgressRequest request) {
        return progressService.markTopicWatched(courseId, topicId, request);
    }
}
