package com.medichub.controller;

import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.RecallQuestionResponse;
import com.medichub.dto.response.RecallSummaryResponse;
import com.medichub.service.RecallViewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Student-side Recalls — <b>view-only</b> study of past questions (not an exam: no start,
 * timer, or submit). The student browses recall papers as cards (filtered by subject/year),
 * then opens one to scroll its questions with the correct answer and explanation revealed.
 */
@RestController
@RequestMapping("/api/student/recalls")
public class StudentRecallController {

    private final RecallViewService recallViewService;

    public StudentRecallController(RecallViewService recallViewService) {
        this.recallViewService = recallViewService;
    }

    /** Recall paper cards, filtered by subject and/or year. */
    @GetMapping
    public PagedResponse<RecallSummaryResponse> papers(@RequestParam(required = false) Long subjectId,
                                                       @RequestParam(required = false) Integer examYear,
                                                       @PageableDefault(size = 12) Pageable pageable) {
        return recallViewService.listPapers(subjectId, examYear, pageable);
    }

    /** One paper's questions (answers revealed), paginated 15/page. */
    @GetMapping("/{id}/questions")
    public PagedResponse<RecallQuestionResponse> questions(@PathVariable Long id,
                                                           @PageableDefault(size = 15) Pageable pageable) {
        return recallViewService.questions(id, pageable);
    }

    /** Distinct exam years available across published recalls — populates the year dropdown. */
    @GetMapping("/years")
    public List<Integer> years() {
        return recallViewService.years();
    }
}
