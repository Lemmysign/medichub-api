package com.medichub.service.impl;

import com.medichub.dto.response.OptionResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.RecallQuestionResponse;
import com.medichub.dto.response.RecallSummaryResponse;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.model.Question;
import com.medichub.model.Subject;
import com.medichub.model.Test;
import com.medichub.model.enums.TestKind;
import com.medichub.repository.QuestionRepository;
import com.medichub.repository.TestRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.RecallViewService;
import com.medichub.service.SubscriptionAccessService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RecallViewServiceImpl implements RecallViewService {

    private final QuestionRepository questionRepository;
    private final TestRepository testRepository;
    private final SubscriptionAccessService subscriptionAccessService;

    public RecallViewServiceImpl(QuestionRepository questionRepository,
                                 TestRepository testRepository,
                                 SubscriptionAccessService subscriptionAccessService) {
        this.questionRepository = questionRepository;
        this.testRepository = testRepository;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Override
    public PagedResponse<RecallSummaryResponse> listPapers(Long subjectId, Integer examYear, Pageable pageable) {
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        Page<Test> page = testRepository.findAvailable(TestKind.RECALL, subjectId, examYear, pageable);
        return PagedResponse.from(page, t -> {
            Subject subject = t.getSubject();
            return new RecallSummaryResponse(
                    t.getId(), t.getTitle(), t.getDescription(),
                    subject == null ? null : subject.getName(), t.getExamYear(),
                    questionRepository.countByTestId(t.getId()));
        });
    }

    @Override
    public PagedResponse<RecallQuestionResponse> questions(Long recallId, Pageable pageable) {
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        Test recall = testRepository.findByIdAndCourseIsNull(recallId)
                .filter(t -> t.getKind() == TestKind.RECALL && t.isPublished())
                .orElseThrow(() -> new ResourceNotFoundException("Recall", recallId));
        Page<Question> page = questionRepository.findByTestIdOrderByOrderIndexAsc(recallId, pageable);
        return PagedResponse.from(page, q -> toResponse(q, recall));
    }

    @Override
    public List<Integer> years() {
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        return testRepository.findPublishedRecallYears();
    }

    private static RecallQuestionResponse toResponse(Question q, Test paper) {
        Subject subject = paper.getSubject();
        List<OptionResponse> options = q.getOptions().stream()
                .map(o -> new OptionResponse(o.getId(), o.getText(), o.isCorrect(), o.getOrderIndex()))
                .toList();
        return new RecallQuestionResponse(
                q.getId(),
                subject == null ? null : subject.getName(),
                paper.getExamYear(),
                paper.getTitle(),
                q.getText(),
                q.getType(),
                q.getExplanation(),
                options);
    }
}
