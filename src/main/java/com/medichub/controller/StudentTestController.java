package com.medichub.controller;

import com.medichub.dto.request.CheckAnswerRequest;
import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.CheckAnswerResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudentTestResponse;
import com.medichub.dto.response.TestResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/student/courses/{courseId}/tests")
public class StudentTestController {

    private final TestAttemptService testAttemptService;

    public StudentTestController(TestAttemptService testAttemptService) {
        this.testAttemptService = testAttemptService;
    }

    @GetMapping
    public List<TestResponse> listTests(@PathVariable Long courseId) {
        return testAttemptService.listCourseTests(courseId);
    }

    @GetMapping("/{testId}")
    public StudentTestResponse getTest(@PathVariable Long courseId, @PathVariable Long testId) {
        return testAttemptService.getTestForStudent(courseId, testId);
    }

    /** Immediate (study) mode: reveal the correct answer + explanation for one question. */
    @PostMapping("/{testId}/questions/{questionId}/check")
    public CheckAnswerResponse check(@PathVariable Long courseId, @PathVariable Long testId,
                                     @PathVariable Long questionId, @RequestBody CheckAnswerRequest request) {
        return testAttemptService.checkAnswer(courseId, testId, questionId, request);
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
