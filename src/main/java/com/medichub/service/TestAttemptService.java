package com.medichub.service;

import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudentTestResponse;
import org.springframework.data.domain.Pageable;

/** Student test-taking — subscription-gated; attempts are kept forever. */
public interface TestAttemptService {

    StudentTestResponse getTestForStudent(Long courseId, Long testId);

    AttemptDetailResponse submit(Long courseId, Long testId, SubmitTestRequest request);

    PagedResponse<AttemptResponse> listMyAttempts(Long courseId, Long testId, Pageable pageable);

    AttemptDetailResponse getAttempt(Long attemptId);
}
