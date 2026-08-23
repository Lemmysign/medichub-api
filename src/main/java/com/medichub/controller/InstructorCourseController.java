package com.medichub.controller;

import com.medichub.dto.request.CreateCourseRequest;
import com.medichub.dto.request.CreateTopicRequest;
import com.medichub.dto.request.ReorderTopicsRequest;
import com.medichub.dto.request.UpdateCourseRequest;
import com.medichub.dto.request.UpdateTopicRequest;
import com.medichub.dto.response.CourseResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.TopicResponse;
import com.medichub.service.CourseService;
import com.medichub.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/courses")
public class InstructorCourseController {

    private final CourseService courseService;
    private final TopicService topicService;

    public InstructorCourseController(CourseService courseService, TopicService topicService) {
        this.courseService = courseService;
        this.topicService = topicService;
    }

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request));
    }

    @GetMapping
    public PagedResponse<CourseResponse> listMine(@PageableDefault(size = 20) Pageable pageable) {
        return courseService.listMyCourses(pageable);
    }

    @GetMapping("/{courseId}")
    public CourseResponse get(@PathVariable Long courseId) {
        return courseService.getOwnedCourse(courseId);
    }

    @PutMapping("/{courseId}")
    public CourseResponse update(@PathVariable Long courseId,
                                 @Valid @RequestBody UpdateCourseRequest request) {
        return courseService.update(courseId, request);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> delete(@PathVariable Long courseId) {
        courseService.delete(courseId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{courseId}/publish")
    public CourseResponse setPublished(@PathVariable Long courseId,
                                       @RequestParam boolean published) {
        return courseService.setPublished(courseId, published);
    }

    // --- Topics ---

    @GetMapping("/{courseId}/topics")
    public List<TopicResponse> listTopics(@PathVariable Long courseId) {
        return topicService.listTopics(courseId);
    }

    @PostMapping("/{courseId}/topics")
    public ResponseEntity<TopicResponse> addTopic(@PathVariable Long courseId,
                                                  @Valid @RequestBody CreateTopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(topicService.addTopic(courseId, request));
    }

    @PutMapping("/{courseId}/topics/{topicId}")
    public TopicResponse updateTopic(@PathVariable Long courseId,
                                     @PathVariable Long topicId,
                                     @Valid @RequestBody UpdateTopicRequest request) {
        return topicService.updateTopic(courseId, topicId, request);
    }

    @DeleteMapping("/{courseId}/topics/{topicId}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long courseId, @PathVariable Long topicId) {
        topicService.deleteTopic(courseId, topicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{courseId}/topics/reorder")
    public List<TopicResponse> reorderTopics(@PathVariable Long courseId,
                                             @Valid @RequestBody ReorderTopicsRequest request) {
        return topicService.reorderTopics(courseId, request);
    }
}
