package com.medichub.controller;

import com.medichub.dto.request.CreateCommentRequest;
import com.medichub.dto.response.CommentResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/courses/{courseId}/comments")
public class StudentCommentController {

    private final CommentService commentService;

    public StudentCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> ask(@PathVariable Long courseId,
                                               @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.postQuestion(courseId, request));
    }

    @GetMapping
    public PagedResponse<CommentResponse> threads(@PathVariable Long courseId,
                                                  @PageableDefault(size = 20) Pageable pageable) {
        return commentService.listCourseThreads(courseId, pageable);
    }
}
