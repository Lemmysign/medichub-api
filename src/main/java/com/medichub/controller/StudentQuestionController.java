package com.medichub.controller;

import com.medichub.dto.response.CommentResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.service.CommentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cross-course feed of the student's own answered questions — powers the notification bell. */
@RestController
@RequestMapping("/api/student/questions")
public class StudentQuestionController {

    private final CommentService commentService;

    public StudentQuestionController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** My questions that have received a reply, newest reply first. */
    @GetMapping
    public PagedResponse<CommentResponse> myAnswered(@PageableDefault(size = 20) Pageable pageable) {
        return commentService.listMyAnsweredQuestions(pageable);
    }
}
