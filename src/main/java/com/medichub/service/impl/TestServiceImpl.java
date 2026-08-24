package com.medichub.service.impl;

import com.medichub.dto.request.CreateQuestionRequest;
import com.medichub.dto.request.CreateTestRequest;
import com.medichub.dto.request.UpdateQuestionRequest;
import com.medichub.dto.request.UpdateTestRequest;
import com.medichub.dto.response.QuestionResponse;
import com.medichub.dto.response.TestResponse;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.TestMapper;
import com.medichub.model.Course;
import com.medichub.model.Question;
import com.medichub.model.Test;
import com.medichub.model.enums.FeedbackMode;
import com.medichub.repository.QuestionRepository;
import com.medichub.repository.TestRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CourseService;
import com.medichub.service.QuestionAuthoring;
import com.medichub.service.TestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final CourseService courseService;
    private final TestMapper testMapper;

    public TestServiceImpl(TestRepository testRepository,
                           QuestionRepository questionRepository,
                           CourseService courseService,
                           TestMapper testMapper) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.courseService = courseService;
        this.testMapper = testMapper;
    }

    @Override
    public TestResponse createTest(Long courseId, CreateTestRequest request) {
        Course course = courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Test test = new Test();
        test.setCourse(course);
        test.setTitle(request.title());
        test.setPassMarkPercent(request.passMarkPercent());
        // Course tests default to immediate (study-mode) feedback.
        test.setFeedbackMode(request.feedbackMode() == null ? FeedbackMode.IMMEDIATE : request.feedbackMode());
        test = testRepository.save(test);
        return testMapper.toTestResponse(test, 0L);
    }

    @Override
    public TestResponse updateTest(Long courseId, Long testId, UpdateTestRequest request) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Test test = requireTest(testId, courseId);
        test.setTitle(request.title());
        test.setPassMarkPercent(request.passMarkPercent());
        if (request.feedbackMode() != null) {
            test.setFeedbackMode(request.feedbackMode());
        }
        return testMapper.toTestResponse(test, questionRepository.countByTestId(testId));
    }

    @Override
    public void deleteTest(Long courseId, Long testId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Test test = requireTest(testId, courseId);
        testRepository.delete(test);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestResponse> listTests(Long courseId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        return testRepository.findByCourseIdOrderByCreatedAtAsc(courseId).stream()
                .map(test -> testMapper.toTestResponse(test, questionRepository.countByTestId(test.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions(Long courseId, Long testId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        requireTest(testId, courseId);
        return testMapper.toQuestions(questionRepository.findByTestIdOrderByOrderIndexAsc(testId));
    }

    @Override
    public QuestionResponse addQuestion(Long courseId, Long testId, CreateQuestionRequest request) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Test test = requireTest(testId, courseId);
        var type = QuestionAuthoring.resolveType(request.type());
        QuestionAuthoring.validate(type, request.options());

        Question question = new Question();
        question.setTest(test);
        question.setText(request.text());
        question.setType(type);
        question.setExplanation(request.explanation());
        question.setOrderIndex(questionRepository.findMaxOrderIndex(testId) + 1);
        QuestionAuthoring.applyOptions(question, request.options());

        question = questionRepository.save(question);
        return testMapper.toQuestion(question);
    }

    @Override
    public List<QuestionResponse> addQuestionsBulk(Long courseId, Long testId, List<CreateQuestionRequest> requests) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Test test = requireTest(testId, courseId);
        int order = questionRepository.findMaxOrderIndex(testId) + 1;
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

    @Override
    public QuestionResponse updateQuestion(Long courseId, Long testId, Long questionId, UpdateQuestionRequest request) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        requireTest(testId, courseId);
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
    public void deleteQuestion(Long courseId, Long testId, Long questionId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        requireTest(testId, courseId);
        Question question = questionRepository.findByIdAndTestId(questionId, testId)
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
        questionRepository.delete(question);
    }

    // ----------------------------------------------------------------------

    private Test requireTest(Long testId, Long courseId) {
        return testRepository.findByIdAndCourseId(testId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", testId));
    }
}
