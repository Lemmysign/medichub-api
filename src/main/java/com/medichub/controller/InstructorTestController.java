package com.medichub.controller;

import com.medichub.dto.request.BulkQuestionsRequest;
import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.CreateTestRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.request.UpdateTestRequest;
import com.medichub.dto.response.QuestionResponse;
import com.medichub.dto.response.TestResponse;
import com.medichub.service.TestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instructor/courses/{courseId}/tests")
public class InstructorTestController {

    private final TestService testService;

    public InstructorTestController(TestService testService) {
        this.testService = testService;
    }

    @PostMapping
    public ResponseEntity<TestResponse> create(@PathVariable Long courseId,
                                               @Valid @RequestBody CreateTestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.createTest(courseId, request));
    }

    @GetMapping
    public List<TestResponse> list(@PathVariable Long courseId) {
        return testService.listTests(courseId);
    }

    @PutMapping("/{testId}")
    public TestResponse update(@PathVariable Long courseId, @PathVariable Long testId,
                               @Valid @RequestBody UpdateTestRequest request) {
        return testService.updateTest(courseId, testId, request);
    }

    @DeleteMapping("/{testId}")
    public ResponseEntity<Void> delete(@PathVariable Long courseId, @PathVariable Long testId) {
        testService.deleteTest(courseId, testId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{testId}/questions")
    public List<QuestionResponse> listQuestions(@PathVariable Long courseId, @PathVariable Long testId) {
        return testService.listQuestions(courseId, testId);
    }

    @PostMapping("/{testId}/questions")
    public ResponseEntity<QuestionResponse> addQuestion(@PathVariable Long courseId, @PathVariable Long testId,
                                                        @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testService.addQuestion(courseId, testId, request));
    }

    @PostMapping("/{testId}/questions/bulk")
    public ResponseEntity<List<QuestionResponse>> addQuestionsBulk(@PathVariable Long courseId, @PathVariable Long testId,
                                                                  @Valid @RequestBody BulkQuestionsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(testService.addQuestionsBulk(courseId, testId, request.questions()));
    }

    @PutMapping("/{testId}/questions/{questionId}")
    public QuestionResponse updateQuestion(@PathVariable Long courseId, @PathVariable Long testId,
                                           @PathVariable Long questionId,
                                           @Valid @RequestBody UpdateQuestionRequest request) {
        return testService.updateQuestion(courseId, testId, questionId, request);
    }

    @DeleteMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long courseId, @PathVariable Long testId,
                                               @PathVariable Long questionId) {
        testService.deleteQuestion(courseId, testId, questionId);
        return ResponseEntity.noContent().build();
    }
}
