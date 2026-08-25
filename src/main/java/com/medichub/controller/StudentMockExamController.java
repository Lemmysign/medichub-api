package com.medichub.controller;

import com.medichub.dto.request.CheckAnswerRequest;
import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.CheckAnswerResponse;
import com.medichub.dto.response.MockExamStartResponse;
import com.medichub.dto.response.MockExamSummaryResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.service.MockExamAttemptService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/mock-exams")
public class StudentMockExamController {

    private final MockExamAttemptService mockExamAttemptService;

    public StudentMockExamController(MockExamAttemptService mockExamAttemptService) {
        this.mockExamAttemptService = mockExamAttemptService;
    }

    @GetMapping
    public PagedResponse<MockExamSummaryResponse> available(@RequestParam(required = false) Long subjectId,
                                                            @PageableDefault(size = 20) Pageable pageable) {
        return mockExamAttemptService.listAvailableMcqs(subjectId, pageable);
    }

    @PostMapping("/{id}/start")
    public MockExamStartResponse start(@PathVariable Long id) {
        return mockExamAttemptService.start(id);
    }

    /** Immediate (study) mode: reveal the correct answer + explanation; pauses a timed clock if wrong. */
    @PostMapping("/{id}/attempts/{attemptId}/questions/{questionId}/check")
    public CheckAnswerResponse check(@PathVariable Long id, @PathVariable Long attemptId,
                                     @PathVariable Long questionId, @RequestBody CheckAnswerRequest request) {
        return mockExamAttemptService.checkAnswer(id, attemptId, questionId, request);
    }

    /** Resume a paused timed attempt after the explanation card is dismissed. */
    @PostMapping("/{id}/attempts/{attemptId}/resume")
    public MockExamStartResponse resume(@PathVariable Long id, @PathVariable Long attemptId) {
        return mockExamAttemptService.resume(id, attemptId);
    }

    @PostMapping("/{id}/attempts/{attemptId}/submit")
    public AttemptDetailResponse submit(@PathVariable Long id, @PathVariable Long attemptId,
                                        @Valid @RequestBody SubmitTestRequest request) {
        return mockExamAttemptService.submit(id, attemptId, request);
    }

    @GetMapping("/{id}/attempts")
    public PagedResponse<AttemptResponse> attempts(@PathVariable Long id, @PageableDefault(size = 20) Pageable pageable) {
        return mockExamAttemptService.listMyAttempts(id, pageable);
    }
}
