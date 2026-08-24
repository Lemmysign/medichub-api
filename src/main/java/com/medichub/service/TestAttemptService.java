package com.medichub.service;

import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudentTestResponse;
import com.medichub.dto.response.TestResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Student test-taking — subscription-gated; attempts are kept forever. */
public interface TestAttemptService {

    /** List a course's tests (metadata only) for a subscribed student. */
    List<TestResponse> listCourseTests(Long courseId);

    StudentTestResponse getTestForStudent(Long courseId, Long testId);

    /** Immediate-mode per-question reveal (course tests are untimed). */
    com.medichub.dto.response.CheckAnswerResponse checkAnswer(
            Long courseId, Long testId, Long questionId, com.medichub.dto.request.CheckAnswerRequest request);

    AttemptDetailResponse submit(Long courseId, Long testId, SubmitTestRequest request);

    PagedResponse<AttemptResponse> listMyAttempts(Long courseId, Long testId, Pageable pageable);

    AttemptDetailResponse getAttempt(Long attemptId);
}
