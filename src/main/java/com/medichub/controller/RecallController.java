package com.medichub.controller;

import com.medichub.dto.request.BulkQuestionsRequest;
import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.CreateRecallRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.request.UpdateRecallRequest;
import com.medichub.dto.response.MockExamResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.QuestionResponse;
import com.medichub.service.MockExamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Creator-side Recall management — instructors and admins. Recalls are past-question papers
 * tagged with a subject and an exam year. The primary authoring path is {@link #create} with
 * questions supplied inline (bulk upload); question CRUD reuses the shared exam engine.
 */
@RestController
@RequestMapping("/api/recalls")
@PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
public class RecallController {

    private final MockExamService mockExamService;

    public RecallController(MockExamService mockExamService) {
        this.mockExamService = mockExamService;
    }

    /** Create a Recall paper, optionally loading its whole question set inline (bulk upload). */
    @PostMapping
    public ResponseEntity<MockExamResponse> create(@Valid @RequestBody CreateRecallRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mockExamService.createRecall(request));
    }

    @GetMapping
    public PagedResponse<MockExamResponse> list(@RequestParam(required = false) Long subjectId,
                                                @RequestParam(required = false) Integer examYear,
                                                @PageableDefault(size = 20) Pageable pageable) {
        return mockExamService.listRecalls(subjectId, examYear, pageable);
    }

    @GetMapping("/{id}")
    public MockExamResponse get(@PathVariable Long id) {
        return mockExamService.get(id);
    }

    @PutMapping("/{id}")
    public MockExamResponse update(@PathVariable Long id, @Valid @RequestBody UpdateRecallRequest request) {
        return mockExamService.updateRecall(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mockExamService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public MockExamResponse setPublished(@PathVariable Long id, @RequestParam boolean published) {
        return mockExamService.setPublished(id, published);
    }

    @GetMapping("/{id}/questions")
    public List<QuestionResponse> listQuestions(@PathVariable Long id) {
        return mockExamService.listQuestions(id);
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<QuestionResponse> addQuestion(@PathVariable Long id,
                                                        @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mockExamService.addQuestion(id, request));
    }

    /** Append more questions to an existing Recall paper in one batch (bulk upload). */
    @PostMapping("/{id}/questions/bulk")
    public ResponseEntity<List<QuestionResponse>> addQuestionsBulk(@PathVariable Long id,
                                                                   @Valid @RequestBody BulkQuestionsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mockExamService.addQuestionsBulk(id, request.questions()));
    }

    @PutMapping("/{id}/questions/{questionId}")
    public QuestionResponse updateQuestion(@PathVariable Long id, @PathVariable Long questionId,
                                           @Valid @RequestBody UpdateQuestionRequest request) {
        return mockExamService.updateQuestion(id, questionId, request);
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        mockExamService.deleteQuestion(id, questionId);
        return ResponseEntity.noContent().build();
    }
}
