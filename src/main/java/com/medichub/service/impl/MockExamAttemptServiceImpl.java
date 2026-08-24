package com.medichub.service.impl;

import com.medichub.dto.request.AnswerSubmission;
import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptAnswerResponse;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.MockExamStartResponse;
import com.medichub.dto.response.MockExamSummaryResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.TestMapper;
import com.medichub.model.AttemptAnswer;
import com.medichub.model.Question;
import com.medichub.model.Test;
import com.medichub.model.TestAttempt;
import com.medichub.model.User;
import com.medichub.repository.QuestionRepository;
import com.medichub.repository.TestAttemptRepository;
import com.medichub.repository.TestRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.MockExamAttemptService;
import com.medichub.service.SubscriptionAccessService;
import com.medichub.service.TestGrader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class MockExamAttemptServiceImpl implements MockExamAttemptService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final UserRepository userRepository;
    private final SubscriptionAccessService subscriptionAccessService;
    private final TestMapper testMapper;

    public MockExamAttemptServiceImpl(TestRepository testRepository,
                                      QuestionRepository questionRepository,
                                      TestAttemptRepository testAttemptRepository,
                                      UserRepository userRepository,
                                      SubscriptionAccessService subscriptionAccessService,
                                      TestMapper testMapper) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.testAttemptRepository = testAttemptRepository;
        this.userRepository = userRepository;
        this.subscriptionAccessService = subscriptionAccessService;
        this.testMapper = testMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MockExamSummaryResponse> listAvailable(Pageable pageable) {
        Long studentId = SecurityUtils.currentUserId();
        subscriptionAccessService.requireActiveAccess(studentId);

        Page<Test> page = testRepository.findByCourseIsNullAndPublishedTrueOrderByCreatedAtDesc(pageable);
        List<Long> ids = page.getContent().stream().map(Test::getId).toList();

        Map<Long, long[]> stats = new HashMap<>(); // testId -> [attemptCount, bestScore]
        if (!ids.isEmpty()) {
            for (Object[] row : testAttemptRepository.attemptStatsByStudentAndTests(studentId, ids)) {
                stats.put((Long) row[0], new long[]{((Number) row[1]).longValue(), ((Number) row[2]).longValue()});
            }
        }

        return PagedResponse.from(page, mock -> {
            long[] s = stats.get(mock.getId());
            Integer best = s == null ? null : (int) s[1];
            long attempts = s == null ? 0 : s[0];
            return new MockExamSummaryResponse(
                    mock.getId(), mock.getTitle(), mock.getDescription(),
                    mock.getPassMarkPercent(), mock.getDurationMinutes(),
                    questionRepository.countByTestId(mock.getId()), best, attempts);
        });
    }

    @Override
    public MockExamStartResponse start(Long mockId) {
        Long studentId = SecurityUtils.currentUserId();
        subscriptionAccessService.requireActiveAccess(studentId);

        Test mock = testRepository.findByIdAndCourseIsNull(mockId)
                .filter(Test::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("Mock exam", mockId));

        TestAttempt attempt = testAttemptRepository
                .findFirstByStudentIdAndTestIdAndSubmittedAtIsNull(studentId, mockId)
                .orElse(null);

        Instant now = Instant.now();
        if (attempt != null) {
            Instant deadline = deadline(attempt, mock);
            if (deadline == null || now.isBefore(deadline)) {
                // Untimed, or still-running timed attempt — resume it.
                return buildStart(attempt, mock);
            }
            // Stale/expired in-progress attempt with no submission — discard and start fresh.
            testAttemptRepository.delete(attempt);
        }

        TestAttempt fresh = new TestAttempt();
        fresh.setTest(mock);
        fresh.setStudent(userRepository.getReferenceById(studentId));
        fresh.setStudentName(userRepository.findById(studentId).map(User::getFullName).orElse(null));
        fresh.setScorePercent(0);
        fresh.setPassed(false);
        fresh.setStartedAt(now);
        fresh = testAttemptRepository.save(fresh);
        return buildStart(fresh, mock);
    }

    @Override
    public AttemptDetailResponse submit(Long mockId, Long attemptId, SubmitTestRequest request) {
        Long studentId = SecurityUtils.currentUserId();
        subscriptionAccessService.requireActiveAccess(studentId);

        TestAttempt attempt = testAttemptRepository.findByIdAndStudentId(attemptId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));
        if (attempt.getTest() == null || !attempt.getTest().getId().equals(mockId)) {
            throw new BadRequestException("Attempt does not belong to this mock exam");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new BadRequestException("This attempt has already been submitted");
        }

        Test mock = attempt.getTest();
        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(mockId);

        Map<Long, Long> selected = new HashMap<>();
        for (AnswerSubmission a : request.answers()) {
            selected.put(a.questionId(), a.selectedOptionId());
        }

        TestGrader.Result result = TestGrader.grade(mock, questions, selected);

        Map<Long, Question> byId = questions.stream().collect(Collectors.toMap(Question::getId, Function.identity()));
        for (TestGrader.QuestionOutcome outcome : result.outcomes()) {
            Question q = byId.get(outcome.questionId());
            AttemptAnswer ans = new AttemptAnswer();
            ans.setAttempt(attempt);
            ans.setQuestion(q);
            ans.setSelectedOption(resolveOption(q, outcome.selectedOptionId()));
            ans.setCorrect(outcome.correct());
            attempt.getAnswers().add(ans);
        }

        attempt.setScorePercent(result.scorePercent());
        attempt.setPassed(result.passed());
        attempt.setSubmittedAt(Instant.now());
        testAttemptRepository.save(attempt);

        return toDetail(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AttemptResponse> listMyAttempts(Long mockId, Pageable pageable) {
        Long studentId = SecurityUtils.currentUserId();
        Page<TestAttempt> page = testAttemptRepository
                .findByStudentIdAndTestIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(studentId, mockId, pageable);
        return PagedResponse.from(page, testMapper::toAttempt);
    }

    @Override
    public com.medichub.dto.response.CheckAnswerResponse checkAnswer(
            Long mockId, Long attemptId, Long questionId, com.medichub.dto.request.CheckAnswerRequest request) {
        Long studentId = SecurityUtils.currentUserId();
        subscriptionAccessService.requireActiveAccess(studentId);

        TestAttempt attempt = testAttemptRepository.findByIdAndStudentId(attemptId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));
        Test mock = attempt.getTest();
        if (mock == null || !mock.getId().equals(mockId)) {
            throw new BadRequestException("Attempt does not belong to this mock exam");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new BadRequestException("This attempt has already been submitted");
        }
        if (mock.getFeedbackMode() != com.medichub.model.enums.FeedbackMode.IMMEDIATE) {
            throw new BadRequestException("This mock exam does not reveal answers during the attempt");
        }

        Question question = questionRepository.findByIdAndTestId(questionId, mockId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        boolean correct = TestAttemptServiceImpl.isSelectedCorrect(question, request.selectedOptionId());

        // Timed mock + wrong answer → freeze the clock while the explanation card is shown.
        boolean timed = mock.getDurationMinutes() != null;
        boolean pause = timed && !correct;
        if (pause && attempt.getPauseStartedAt() == null) {
            attempt.setPauseStartedAt(Instant.now());
            testAttemptRepository.save(attempt);
        }
        Instant expiresAt = pause ? null : deadline(attempt, mock);
        return new com.medichub.dto.response.CheckAnswerResponse(
                questionId, correct, TestAttemptServiceImpl.firstCorrectOptionId(question),
                question.getExplanation(), pause, expiresAt);
    }

    @Override
    public MockExamStartResponse resume(Long mockId, Long attemptId) {
        Long studentId = SecurityUtils.currentUserId();
        subscriptionAccessService.requireActiveAccess(studentId);

        TestAttempt attempt = testAttemptRepository.findByIdAndStudentId(attemptId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));
        Test mock = attempt.getTest();
        if (mock == null || !mock.getId().equals(mockId)) {
            throw new BadRequestException("Attempt does not belong to this mock exam");
        }
        if (attempt.getSubmittedAt() != null) {
            throw new BadRequestException("This attempt has already been submitted");
        }
        if (attempt.getPauseStartedAt() != null) {
            long elapsed = Duration.between(attempt.getPauseStartedAt(), Instant.now()).getSeconds();
            attempt.setPausedSeconds(attempt.getPausedSeconds() + (int) Math.max(0, elapsed));
            attempt.setPauseStartedAt(null);
            testAttemptRepository.save(attempt);
        }
        return buildStart(attempt, mock);
    }

    // ----------------------------------------------------------------------

    /** Deadline for a timed mock (startedAt + duration + any paused time); null for untimed. */
    private Instant deadline(TestAttempt attempt, Test mock) {
        if (mock.getDurationMinutes() == null) {
            return null;
        }
        return attempt.getStartedAt()
                .plus(Duration.ofMinutes(mock.getDurationMinutes()))
                .plusSeconds(attempt.getPausedSeconds());
    }

    private MockExamStartResponse buildStart(TestAttempt attempt, Test mock) {
        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(mock.getId());
        return new MockExamStartResponse(
                attempt.getId(),
                mock.getId(),
                mock.getTitle(),
                mock.getPassMarkPercent(),
                mock.getDurationMinutes(),
                mock.getFeedbackMode(),
                attempt.getStartedAt(),
                deadline(attempt, mock),
                testMapper.toStudentQuestions(questions));
    }

    private com.medichub.model.QuestionOption resolveOption(Question question, Long optionId) {
        if (question == null || optionId == null) {
            return null;
        }
        return question.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElse(null);
    }

    private AttemptDetailResponse toDetail(TestAttempt attempt) {
        List<AttemptAnswerResponse> answers = attempt.getAnswers().stream()
                .map(a -> new AttemptAnswerResponse(
                        a.getQuestion().getId(),
                        a.getQuestion().getText(),
                        a.getSelectedOption() == null ? null : a.getSelectedOption().getId(),
                        TestAttemptServiceImpl.firstCorrectOptionId(a.getQuestion()),
                        a.getQuestion().getExplanation(),
                        a.isCorrect()))
                .toList();
        return new AttemptDetailResponse(
                attempt.getId(),
                attempt.getTest().getId(),
                attempt.getScorePercent(),
                attempt.isPassed(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                answers);
    }
}
