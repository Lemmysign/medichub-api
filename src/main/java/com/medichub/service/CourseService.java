package com.medichub.service;

import com.medichub.dto.request.CreateCourseRequest;
import com.medichub.dto.request.UpdateCourseRequest;
import com.medichub.dto.response.CoursePreviewResponse;
import com.medichub.dto.response.CourseResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.model.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface CourseService {

    CourseResponse create(CreateCourseRequest request);

    /** Instructor: upload/replace the course thumbnail (stored on R2). */
    CourseResponse uploadThumbnail(Long courseId, MultipartFile file);

    CourseResponse update(Long courseId, UpdateCourseRequest request);

    void delete(Long courseId);

    CourseResponse setPublished(Long courseId, boolean published);

    CourseResponse getOwnedCourse(Long courseId);

    PagedResponse<CourseResponse> listMyCourses(Pageable pageable);

    PagedResponse<CourseResponse> listPublished(Pageable pageable);

    CoursePreviewResponse getPublicPreview(Long courseId);

    // --- service-tier helpers: entities stay within the service layer, never returned to controllers ---

    /** Loads a course and asserts the given instructor owns it, else 404/403. */
    Course requireOwnedCourse(Long courseId, Long instructorId);

    /** Loads a PUBLISHED course or 404 (used by content-access flows). */
    Course requirePublishedCourse(Long courseId);

    /** Loads any course by id or 404. */
    Course requireCourse(Long courseId);
}
