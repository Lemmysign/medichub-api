package com.medichub.controller;

import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudentTestResponse;
import com.medichub.service.TestAttemptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/courses/{courseId}/tests")
public class StudentTestController {

    private final TestAttemptService testAttemptService;

    public StudentTestController(TestAttemptService testAttemptService) {
        this.testAttemptService = testAttemptService;
    }

    @GetMapping("/{testId}")
    public StudentTestResponse getTest(@PathVariable Long courseId, @PathVariable Long testId) {
        return testAttemptService.getTestForStudent(courseId, testId);
    }

    @PostMapping("/{testId}/submit")
    public AttemptDetailResponse submit(@PathVariable Long courseId, @PathVariable Long testId,
                                        @Valid @RequestBody SubmitTestRequest request) {
        return testAttemptService.submit(courseId, testId, request);
    }

    @GetMapping("/{testId}/attempts")
    public PagedResponse<AttemptResponse> myAttempts(@PathVariable Long courseId, @PathVariable Long testId,
                                                     @PageableDefault(size = 20) Pageable pageable) {
        return testAttemptService.listMyAttempts(courseId, testId, pageable);
    }
}
