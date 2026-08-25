package com.medichub.service.impl;

import com.medichub.dto.request.CreateMockExamRequest;
import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.CreateRecallRequest;
import com.medichub.dto.request.UpdateMockExamRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.request.UpdateRecallRequest;
import com.medichub.dto.response.MockExamResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.QuestionResponse;
import com.medichub.exception.AccessDeniedException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.TestMapper;
import com.medichub.model.Question;
import com.medichub.model.Subject;
import com.medichub.model.Test;
import com.medichub.model.User;
import com.medichub.model.enums.FeedbackMode;
import com.medichub.model.enums.TestKind;
import com.medichub.repository.QuestionRepository;
import com.medichub.repository.SubjectRepository;
import com.medichub.repository.TestRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.MockExamService;
import com.medichub.service.QuestionAuthoring;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MockExamServiceImpl implements MockExamService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TestMapper testMapper;

    public MockExamServiceImpl(TestRepository testRepository,
                               QuestionRepository questionRepository,
                               UserRepository userRepository,
                               SubjectRepository subjectRepository,
                               TestMapper testMapper) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.testMapper = testMapper;
    }

    // ----------------------------------------------------------------- MCQs

    @Override
    public MockExamResponse create(CreateMockExamRequest request) {
        User owner = userRepository.getReferenceById(SecurityUtils.currentUserId());
        Test mcq = new Test();
        mcq.setCourse(null);
        mcq.setOwner(owner);
        mcq.setKind(TestKind.MCQ);
        mcq.setSubject(requireSubject(request.subjectId()));
        mcq.setTitle(request.title());
        mcq.setDescription(request.description());
        mcq.setPassMarkPercent(request.passMarkPercent());
        mcq.setDurationMinutes(request.durationMinutes()); // null = untimed
        mcq.setPublished(false);
        // MCQs default to exam-style (reveal only after submission).
        mcq.setFeedbackMode(request.feedbackMode() == null ? FeedbackMode.ON_SUBMISSION : request.feedbackMode());
        mcq = testRepository.save(mcq);
        return toResponse(mcq, 0L);
    }

    @Override
    public MockExamResponse update(Long mockId, UpdateMockExamRequest request) {
        Test mcq = requireManageable(mockId, TestKind.MCQ);
        mcq.setSubject(requireSubject(request.subjectId()));
        mcq.setTitle(request.title());
        mcq.setDescription(request.description());
        mcq.setPassMarkPercent(request.passMarkPercent());
        mcq.setDurationMinutes(request.durationMinutes()); // null = untimed
        if (request.feedbackMode() != null) {
            mcq.setFeedbackMode(request.feedbackMode());
        }
        return toResponse(mcq, questionRepository.countByTestId(mockId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MockExamResponse> listMcqs(Long subjectId, Pageable pageable) {
        Long ownerId = SecurityUtils.isAdmin() ? null : SecurityUtils.currentUserId();
        var page = testRepository.findManageable(TestKind.MCQ, ownerId, subjectId, null, pageable);
        return PagedResponse.from(page, m -> toResponse(m, questionRepository.countByTestId(m.getId())));
    }

    // -------------------------------------------------------------- Recalls

    @Override
    public MockExamResponse createRecall(CreateRecallRequest request) {
        User owner = userRepository.getReferenceById(SecurityUtils.currentUserId());
        Test recall = new Test();
        recall.setCourse(null);
        recall.setOwner(owner);
        recall.setKind(TestKind.RECALL);
        recall.setSubject(requireSubject(request.subjectId()));
        recall.setExamYear(request.examYear());
        recall.setTitle(request.title());
        recall.setDescription(request.description());
        recall.setPublished(false);
        // Recalls are view-only study material, not exams — these fields are unused but non-null.
        recall.setPassMarkPercent(0);
        recall.setDurationMinutes(null);
        recall.setFeedbackMode(FeedbackMode.ON_SUBMISSION);
        recall = testRepository.save(recall);

        long count = 0;
        if (request.questions() != null && !request.questions().isEmpty()) {
            appendQuestions(recall, 1, request.questions());
            count = request.questions().size();
        }
        return toResponse(recall, count);
    }

    @Override
    public MockExamResponse updateRecall(Long recallId, UpdateRecallRequest request) {
        Test recall = requireManageable(recallId, TestKind.RECALL);
        recall.setSubject(requireSubject(request.subjectId()));
        recall.setExamYear(request.examYear());
        recall.setTitle(request.title());
        recall.setDescription(request.description());
        return toResponse(recall, questionRepository.countByTestId(recallId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MockExamResponse> listRecalls(Long subjectId, Integer examYear, Pageable pageable) {
        Long ownerId = SecurityUtils.isAdmin() ? null : SecurityUtils.currentUserId();
        var page = testRepository.findManageable(TestKind.RECALL, ownerId, subjectId, examYear, pageable);
        return PagedResponse.from(page, m -> toResponse(m, questionRepository.countByTestId(m.getId())));
    }

    // --------------------------------------------------- Shared operations

    @Override
    public void delete(Long testId) {
        testRepository.delete(requireManageable(testId, null));
    }

    @Override
    public MockExamResponse setPublished(Long testId, boolean published) {
        Test test = requireManageable(testId, null);
        test.setPublished(published);
        return toResponse(test, questionRepository.countByTestId(testId));
    }

    @Override
    @Transactional(readOnly = true)
    public MockExamResponse get(Long testId) {
        Test test = requireManageable(testId, null);
        return toResponse(test, questionRepository.countByTestId(testId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions(Long testId) {
        requireManageable(testId, null);
        return testMapper.toQuestions(questionRepository.findByTestIdOrderByOrderIndexAsc(testId));
    }

    @Override
    public QuestionResponse addQuestion(Long testId, CreateQuestionRequest request) {
        Test test = requireManageable(testId, null);
        var type = QuestionAuthoring.resolveType(request.type());
        QuestionAuthoring.validate(type, request.options());
        Question question = new Question();
        question.setTest(test);
        question.setText(request.text());
        question.setType(type);
        question.setExplanation(request.explanation());
        question.setOrderIndex(questionRepository.findMaxOrderIndex(testId) + 1);
        QuestionAuthoring.applyOptions(question, request.options());
        return testMapper.toQuestion(questionRepository.save(question));
    }

    @Override
    public List<QuestionResponse> addQuestionsBulk(Long testId, List<CreateQuestionRequest> requests) {
        Test test = requireManageable(testId, null);
        int order = questionRepository.findMaxOrderIndex(testId) + 1;
        return appendQuestions(test, order, requests);
    }

    @Override
    public QuestionResponse updateQuestion(Long testId, Long questionId, UpdateQuestionRequest request) {
        requireManageable(testId, null);
        Question question = questionRepository.findByIdAndTestId(questionId, testId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        var type = QuestionAuthoring.resolveType(request.type());
        QuestionAuthoring.validate(type, request.options());
        question.setText(request.text());
        question.setType(type);
        question.setExplanation(request.explanation());
        QuestionAuthoring.reconcileOptions(question, request.options()); // in-place: keeps ids referenced by past attempts
        return testMapper.toQuestion(question);
    }

    @Override
    public void deleteQuestion(Long testId, Long questionId) {
        requireManageable(testId, null);
        Question question = questionRepository.findByIdAndTestId(questionId, testId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        questionRepository.delete(question);
    }

    // ----------------------------------------------------------------------

    /** Saves a batch of questions onto {@code test} starting at {@code startOrder}. */
    private List<QuestionResponse> appendQuestions(Test test, int startOrder, List<CreateQuestionRequest> requests) {
        int order = startOrder;
        List<QuestionResponse> saved = new java.util.ArrayList<>();
        for (CreateQuestionRequest request : requests) {
            var type = QuestionAuthoring.resolveType(request.type());
            QuestionAuthoring.validate(type, request.options());
            Question question = new Question();
            question.setTest(test);
            question.setText(request.text());
            question.setType(type);
            question.setExplanation(request.explanation());
            question.setOrderIndex(order++);
            QuestionAuthoring.applyOptions(question, request.options());
            saved.add(testMapper.toQuestion(questionRepository.save(question)));
        }
        return saved;
    }

    private Subject requireSubject(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));
    }

    /**
     * Loads a standalone test the current user may manage: admin → any; instructor → own.
     * When {@code expectedKind} is non-null the test must be of that kind (guards MCQ vs Recall endpoints).
     */
    private Test requireManageable(Long testId, TestKind expectedKind) {
        Test test = testRepository.findByIdAndCourseIsNull(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", testId));
        if (expectedKind != null && test.getKind() != expectedKind) {
            throw new ResourceNotFoundException("Exam", testId);
        }
        if (SecurityUtils.isAdmin()) {
            return test;
        }
        if (test.getOwner() == null || !test.getOwner().getId().equals(SecurityUtils.currentUserId())) {
            throw new AccessDeniedException("You do not own this exam");
        }
        return test;
    }

    private MockExamResponse toResponse(Test test, long questionCount) {
        String ownerName = test.getOwner() == null ? null : test.getOwner().getFullName();
        Subject subject = test.getSubject();
        return new MockExamResponse(test.getId(), test.getTitle(), test.getDescription(),
                test.getPassMarkPercent(), test.getDurationMinutes(), test.isPublished(),
                test.getFeedbackMode(), ownerName, questionCount, test.getKind(),
                subject == null ? null : subject.getId(),
                subject == null ? null : subject.getName(),
                test.getExamYear());
    }
}
