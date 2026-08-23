package com.medichub.service.impl;

import com.medichub.dto.request.AnswerSubmission;
import com.medichub.dto.request.SubmitTestRequest;
import com.medichub.dto.response.AttemptAnswerResponse;
import com.medichub.dto.response.AttemptDetailResponse;
import com.medichub.dto.response.AttemptResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudentTestResponse;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.TestMapper;
import com.medichub.model.AttemptAnswer;
import com.medichub.model.Question;
import com.medichub.model.QuestionOption;
import com.medichub.model.Test;
import com.medichub.model.TestAttempt;
import com.medichub.repository.QuestionRepository;
import com.medichub.repository.TestAttemptRepository;
import com.medichub.repository.TestRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CourseService;
import com.medichub.service.SubscriptionAccessService;
import com.medichub.service.TestAttemptService;
import com.medichub.service.TestGrader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestAttemptServiceImpl implements TestAttemptService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final SubscriptionAccessService subscriptionAccessService;
    private final TestMapper testMapper;

    public TestAttemptServiceImpl(TestRepository testRepository,
                                  QuestionRepository questionRepository,
                                  TestAttemptRepository testAttemptRepository,
                                  UserRepository userRepository,
                                  CourseService courseService,
                                  SubscriptionAccessService subscriptionAccessService,
                                  TestMapper testMapper) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.testAttemptRepository = testAttemptRepository;
        this.userRepository = userRepository;
        this.courseService = courseService;
        this.subscriptionAccessService = subscriptionAccessService;
        this.testMapper = testMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentTestResponse getTestForStudent(Long courseId, Long testId) {
        Long studentId = SecurityUtils.currentUserId();
        courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(studentId);

        Test test = requireTest(testId, courseId);
        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(testId);
        return testMapper.toStudentTest(test, questions);
    }

    @Override
    public AttemptDetailResponse submit(Long courseId, Long testId, SubmitTestRequest request) {
        Long studentId = SecurityUtils.currentUserId();
        courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(studentId);

        Test test = requireTest(testId, courseId);
        List<Question> questions = questionRepository.findByTestIdOrderByOrderIndexAsc(testId);

        // Last submission for a given question wins if duplicated.
        Map<Long, Long> selectedByQuestion = new HashMap<>();
        for (AnswerSubmission answer : request.answers()) {
            selectedByQuestion.put(answer.questionId(), answer.selectedOptionId());
        }

        TestGrader.Result result = TestGrader.grade(test, questions, selectedByQuestion);

        Instant now = Instant.now();
        TestAttempt attempt = new TestAttempt();
        attempt.setTest(test);
        attempt.setStudent(userRepository.getReferenceById(studentId));
        attempt.setStudentName(userRepository.findById(studentId).map(u -> u.getFullName()).orElse(null));
        attempt.setScorePercent(result.scorePercent());
        attempt.setPassed(result.passed());
        attempt.setStartedAt(now);
        attempt.setSubmittedAt(now);

        Map<Long, Question> questionById = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        for (TestGrader.QuestionOutcome outcome : result.outcomes()) {
            Question question = questionById.get(outcome.questionId());
            AttemptAnswer answer = new AttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(resolveOption(question, outcome.selectedOptionId()));
            answer.setCorrect(outcome.correct());
            attempt.getAnswers().add(answer);
        }

        attempt = testAttemptRepository.save(attempt);
        return toDetail(attempt);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AttemptResponse> listMyAttempts(Long courseId, Long testId, Pageable pageable) {
        Long studentId = SecurityUtils.currentUserId();
        requireTest(testId, courseId);
        Page<TestAttempt> page = testAttemptRepository
                .findByStudentIdAndTestIdOrderBySubmittedAtDesc(studentId, testId, pageable);
        return PagedResponse.from(page, testMapper::toAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptDetailResponse getAttempt(Long attemptId) {
        Long studentId = SecurityUtils.currentUserId();
        TestAttempt attempt = testAttemptRepository.findWithAnswersByIdAndStudentId(attemptId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));
        return toDetail(attempt);
    }

    // ----------------------------------------------------------------------

    private Test requireTest(Long testId, Long courseId) {
        return testRepository.findByIdAndCourseId(testId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", testId));
    }

    private QuestionOption resolveOption(Question question, Long optionId) {
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
