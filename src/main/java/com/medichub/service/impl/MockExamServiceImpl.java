package com.medichub.service.impl;

import com.medichub.dto.request.CreateMockExamRequest;
import com.medichub.dto.request.CreateOptionRequest;
import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.UpdateMockExamRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.response.MockExamResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.QuestionResponse;
import com.medichub.exception.AccessDeniedException;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.TestMapper;
import com.medichub.model.Question;
import com.medichub.model.QuestionOption;
import com.medichub.model.Test;
import com.medichub.model.User;
import com.medichub.model.enums.QuestionType;
import com.medichub.repository.QuestionRepository;
import com.medichub.repository.TestRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.MockExamService;
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
    private final TestMapper testMapper;

    public MockExamServiceImpl(TestRepository testRepository,
                               QuestionRepository questionRepository,
                               UserRepository userRepository,
                               TestMapper testMapper) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.testMapper = testMapper;
    }

    @Override
    public MockExamResponse create(CreateMockExamRequest request) {
        User owner = userRepository.getReferenceById(SecurityUtils.currentUserId());
        Test mock = new Test();
        mock.setCourse(null);
        mock.setOwner(owner);
        mock.setTitle(request.title());
        mock.setDescription(request.description());
        mock.setPassMarkPercent(request.passMarkPercent());
        mock.setDurationMinutes(request.durationMinutes());
        mock.setPublished(false);
        mock = testRepository.save(mock);
        return toResponse(mock, 0L);
    }

    @Override
    public MockExamResponse update(Long mockId, UpdateMockExamRequest request) {
        Test mock = requireManageable(mockId);
        mock.setTitle(request.title());
        mock.setDescription(request.description());
        mock.setPassMarkPercent(request.passMarkPercent());
        mock.setDurationMinutes(request.durationMinutes());
        return toResponse(mock, questionRepository.countByTestId(mockId));
    }

    @Override
    public void delete(Long mockId) {
        testRepository.delete(requireManageable(mockId));
    }

    @Override
    public MockExamResponse setPublished(Long mockId, boolean published) {
        Test mock = requireManageable(mockId);
        mock.setPublished(published);
        return toResponse(mock, questionRepository.countByTestId(mockId));
    }

    @Override
    @Transactional(readOnly = true)
    public MockExamResponse get(Long mockId) {
        Test mock = requireManageable(mockId);
        return toResponse(mock, questionRepository.countByTestId(mockId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MockExamResponse> list(Pageable pageable) {
        var page = SecurityUtils.isAdmin()
                ? testRepository.findByCourseIsNullOrderByCreatedAtDesc(pageable)
                : testRepository.findByCourseIsNullAndOwnerIdOrderByCreatedAtDesc(SecurityUtils.currentUserId(), pageable);
        return PagedResponse.from(page, m -> toResponse(m, questionRepository.countByTestId(m.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions(Long mockId) {
        requireManageable(mockId);
        return testMapper.toQuestions(questionRepository.findByTestIdOrderByOrderIndexAsc(mockId));
    }

    @Override
    public QuestionResponse addQuestion(Long mockId, CreateQuestionRequest request) {
        Test mock = requireManageable(mockId);
        validateHasCorrectOption(request.options());
        Question question = new Question();
        question.setTest(mock);
        question.setText(request.text());
        question.setType(request.type() == null ? QuestionType.MULTIPLE_CHOICE : request.type());
        question.setOrderIndex(questionRepository.findMaxOrderIndex(mockId) + 1);
        applyOptions(question, request.options());
        return testMapper.toQuestion(questionRepository.save(question));
    }

    @Override
    public QuestionResponse updateQuestion(Long mockId, Long questionId, UpdateQuestionRequest request) {
        requireManageable(mockId);
        Question question = questionRepository.findByIdAndTestId(questionId, mockId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        validateHasCorrectOption(request.options());
        question.setText(request.text());
        question.setType(request.type() == null ? QuestionType.MULTIPLE_CHOICE : request.type());
        question.getOptions().clear();
        applyOptions(question, request.options());
        return testMapper.toQuestion(question);
    }

    @Override
    public void deleteQuestion(Long mockId, Long questionId) {
        requireManageable(mockId);
        Question question = questionRepository.findByIdAndTestId(questionId, mockId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        questionRepository.delete(question);
    }

    // ----------------------------------------------------------------------

    /** Loads a mock (course-less test) the current user may manage: admin → any; instructor → own. */
    private Test requireManageable(Long mockId) {
        Test mock = testRepository.findByIdAndCourseIsNull(mockId)
                .orElseThrow(() -> new ResourceNotFoundException("Mock exam", mockId));
        if (SecurityUtils.isAdmin()) {
            return mock;
        }
        if (mock.getOwner() == null || !mock.getOwner().getId().equals(SecurityUtils.currentUserId())) {
            throw new AccessDeniedException("You do not own this mock exam");
        }
        return mock;
    }

    private MockExamResponse toResponse(Test mock, long questionCount) {
        String ownerName = mock.getOwner() == null ? null : mock.getOwner().getFullName();
        return new MockExamResponse(mock.getId(), mock.getTitle(), mock.getDescription(),
                mock.getPassMarkPercent(), mock.getDurationMinutes(), mock.isPublished(), ownerName, questionCount);
    }

    private void validateHasCorrectOption(List<CreateOptionRequest> options) {
        if (options.stream().noneMatch(o -> Boolean.TRUE.equals(o.correct()))) {
            throw new BadRequestException("A question must have at least one correct option");
        }
    }

    private void applyOptions(Question question, List<CreateOptionRequest> options) {
        for (int i = 0; i < options.size(); i++) {
            CreateOptionRequest req = options.get(i);
            QuestionOption option = new QuestionOption();
            option.setQuestion(question);
            option.setText(req.text());
            option.setCorrect(Boolean.TRUE.equals(req.correct()));
            option.setOrderIndex(i);
            question.getOptions().add(option);
        }
    }
}
