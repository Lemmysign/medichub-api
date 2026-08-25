package com.medichub.service;

import com.medichub.dto.request.CreateMockExamRequest;
import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.CreateRecallRequest;
import com.medichub.dto.request.UpdateMockExamRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.request.UpdateRecallRequest;
import com.medichub.dto.response.MockExamResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.QuestionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Creator-side (instructor or admin) management of standalone exams — both MCQ practice
 * exams and Recall papers. Question management is shared across both kinds.
 */
public interface MockExamService {

    // --- MCQ practice exams ---
    MockExamResponse create(CreateMockExamRequest request);

    MockExamResponse update(Long mockId, UpdateMockExamRequest request);

    /** Admin sees all MCQs; an instructor sees only their own. Optional subject filter. */
    PagedResponse<MockExamResponse> listMcqs(Long subjectId, Pageable pageable);

    // --- Recall papers ---

    /** Create a Recall paper, optionally loading its questions inline (bulk upload). */
    MockExamResponse createRecall(CreateRecallRequest request);

    MockExamResponse updateRecall(Long recallId, UpdateRecallRequest request);

    /** Admin sees all Recalls; an instructor sees only their own. Optional subject/year filters. */
    PagedResponse<MockExamResponse> listRecalls(Long subjectId, Integer examYear, Pageable pageable);

    // --- Shared across MCQs and Recalls ---
    void delete(Long testId);

    MockExamResponse setPublished(Long testId, boolean published);

    MockExamResponse get(Long testId);

    List<QuestionResponse> listQuestions(Long testId);

    QuestionResponse addQuestion(Long testId, CreateQuestionRequest request);

    /** Append a batch of questions in one transaction (all-or-nothing). */
    List<QuestionResponse> addQuestionsBulk(Long testId, List<CreateQuestionRequest> requests);

    QuestionResponse updateQuestion(Long testId, Long questionId, UpdateQuestionRequest request);

    void deleteQuestion(Long testId, Long questionId);
}
