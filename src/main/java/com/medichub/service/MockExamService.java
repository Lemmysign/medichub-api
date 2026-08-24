package com.medichub.service;

import com.medichub.dto.request.CreateMockExamRequest;
import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.UpdateMockExamRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.response.MockExamResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.QuestionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Creator-side (instructor or admin) management of standalone mock exams. */
public interface MockExamService {

    MockExamResponse create(CreateMockExamRequest request);

    MockExamResponse update(Long mockId, UpdateMockExamRequest request);

    void delete(Long mockId);

    MockExamResponse setPublished(Long mockId, boolean published);

    MockExamResponse get(Long mockId);

    /** Admin sees all mocks; an instructor sees only their own. */
    PagedResponse<MockExamResponse> list(Pageable pageable);

    List<QuestionResponse> listQuestions(Long mockId);

    QuestionResponse addQuestion(Long mockId, CreateQuestionRequest request);

    /** Append a batch of questions in one transaction (all-or-nothing). */
    List<QuestionResponse> addQuestionsBulk(Long mockId, List<CreateQuestionRequest> requests);

    QuestionResponse updateQuestion(Long mockId, Long questionId, UpdateQuestionRequest request);

    void deleteQuestion(Long mockId, Long questionId);
}
