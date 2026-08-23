package com.medichub.service;

import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.CreateTestRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.request.UpdateTestRequest;
import com.medichub.dto.response.QuestionResponse;
import com.medichub.dto.response.TestResponse;

import java.util.List;

/** Instructor test authoring — all ownership-checked via the owning course. */
public interface TestService {

    TestResponse createTest(Long courseId, CreateTestRequest request);

    TestResponse updateTest(Long courseId, Long testId, UpdateTestRequest request);

    void deleteTest(Long courseId, Long testId);

    List<TestResponse> listTests(Long courseId);

    List<QuestionResponse> listQuestions(Long courseId, Long testId);

    QuestionResponse addQuestion(Long courseId, Long testId, CreateQuestionRequest request);

    QuestionResponse updateQuestion(Long courseId, Long testId, Long questionId, UpdateQuestionRequest request);

    void deleteQuestion(Long courseId, Long testId, Long questionId);
}
