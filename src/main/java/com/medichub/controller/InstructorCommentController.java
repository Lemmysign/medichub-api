package com.medichub.controller;

import com.medichub.dto.request.CreateReplyRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructor")
public class InstructorCommentController {

    private final CommentService commentService;

    public InstructorCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** Questions across the instructor's courses; pass unansweredOnly=true to triage. */
    @GetMapping("/questions")
    public PagedResponse<CommentResponse> questions(
            @RequestParam(defaultValue = "false") boolean unansweredOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        return commentService.listInstructorQuestions(unansweredOnly, pageable);
    }

    @PostMapping("/courses/{courseId}/comments/{commentId}/reply")
    public ResponseEntity<CommentResponse> reply(@PathVariable Long courseId,
                                                 @PathVariable Long commentId,
                                                 @Valid @RequestBody CreateReplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.postReply(courseId, commentId, request));
    }
}
