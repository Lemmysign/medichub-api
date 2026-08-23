package com.medichub.controller;

import com.medichub.dto.response.CoursePreviewResponse;
import com.medichub.dto.response.CourseResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.service.CourseService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Open catalog browsing — no auth required (CLAUDE.md §3). */
@RestController
@RequestMapping("/api/public/courses")
public class PublicCourseController {

    private final CourseService courseService;

    public PublicCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public PagedResponse<CourseResponse> listPublished(@PageableDefault(size = 20) Pageable pageable) {
        return courseService.listPublished(pageable);
    }

    @GetMapping("/{courseId}")
    public CoursePreviewResponse preview(@PathVariable Long courseId) {
        return courseService.getPublicPreview(courseId);
    }
}
