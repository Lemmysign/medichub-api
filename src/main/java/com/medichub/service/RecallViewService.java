package com.medichub.service;

import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.RecallQuestionResponse;
import com.medichub.dto.response.RecallSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Student-side <b>view-only</b> access to Recalls (past questions). Subscription-gated.
 * Recalls are studied, not taken: the student browses recall <em>papers</em> as cards, then opens
 * one to read through its questions with answers revealed.
 */
public interface RecallViewService {

    /** Published recall papers as cards, filtered by subject and/or year. */
    PagedResponse<RecallSummaryResponse> listPapers(Long subjectId, Integer examYear, Pageable pageable);

    /** One published recall paper's questions (answers revealed), paginated. */
    PagedResponse<RecallQuestionResponse> questions(Long recallId, Pageable pageable);

    /** Distinct exam years across published recalls, newest first — for the year dropdown. */
    List<Integer> years();
}
