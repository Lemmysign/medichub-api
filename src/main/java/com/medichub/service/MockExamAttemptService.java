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

    AttemptDetailResponse submit(Long mockId, Long attemptId, SubmitTestRequest request);

    PagedResponse<AttemptResponse> listMyAttempts(Long mockId, Pageable pageable);
}
