package com.medichub.service;

import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.MockExamStartResponse;
import com.medichub.dto.response.MockExamSummaryResponse;
import com.medichub.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

/** Student-side: discover, start (timed), and submit mock exams. Subscription-gated. */
public interface MockExamAttemptService {

    PagedResponse<MockExamSummaryResponse> listAvailable(Pageable pageable);

    /** Start (or resume) a timed attempt; the server anchors the clock. */
    MockExamStartResponse start(Long mockId);

    /**
     * Immediate-mode per-question reveal. For a timed mock, a wrong answer pauses the
     * server clock (the explanation card is up) until {@link #resume} is called.
     */
    com.medichub.dto.response.CheckAnswerResponse checkAnswer(
            Long mockId, Long attemptId, Long questionId, com.medichub.dto.request.CheckAnswerRequest request);

    /** Resume a paused timed attempt after the student dismisses the explanation card. */
    MockExamStartResponse resume(Long mockId, Long attemptId);

    AttemptDetailResponse submit(Long mockId, Long attemptId, SubmitTestRequest request);

    PagedResponse<AttemptResponse> listMyAttempts(Long mockId, Pageable pageable);
}
