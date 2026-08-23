package com.medichub.controller;

import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.service.TestAttemptService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Referenceable attempt history (CLAUDE.md §5.3) — a student's own attempt detail. */
@RestController
@RequestMapping("/api/student/attempts")
public class StudentAttemptController {

    private final TestAttemptService testAttemptService;

    public StudentAttemptController(TestAttemptService testAttemptService) {
        this.testAttemptService = testAttemptService;
    }

    @GetMapping("/{attemptId}")
    public AttemptDetailResponse getAttempt(@PathVariable Long attemptId) {
        return testAttemptService.getAttempt(attemptId);
    }
}
