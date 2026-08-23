package com.medichub.service;

import com.medichub.dto.response.CourseProgressResponse;
import com.medichub.dto.response.EnrolledCourseResponse;
import com.medichub.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    /** Student opens a course: enroll idempotently, bump lastAccessedAt, return progress. Gated. */
    CourseProgressResponse openCourse(Long courseId);

    /** Ensure the current student is enrolled in the course and touch lastAccessedAt. */
    void ensureEnrolled(Long courseId);

    /** The student's enrolled courses with per-course progress %. */
    PagedResponse<EnrolledCourseResponse> listMyCourses(Pageable pageable);
}
