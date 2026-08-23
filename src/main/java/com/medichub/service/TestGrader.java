package com.medichub.service;

import com.medichub.model.Question;
import com.medichub.model.QuestionOption;
import com.medichub.model.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Pure auto-grading logic (no persistence), so it is fully unit-testable.
 *
 * <p>Single-selection grading: a question is correct iff the option the student
 * selected belongs to that question AND is flagged correct. This covers
 * SINGLE_CHOICE and TRUE_FALSE, and treats MULTIPLE_CHOICE as single-best-answer
 * (the data model stores one selectedOption per answer). True multi-select grading
 * is a deferred enhancement.
 */
public final class TestGrader {

    private TestGrader() {
    }

    public record QuestionOutcome(Long questionId, Long selectedOptionId, boolean correct) {
    }

    public record Result(int scorePercent, boolean passed, List<QuestionOutcome> outcomes) {
    }

    public static Result grade(Test test, List<Question> questions, Map<Long, Long> selectedOptionByQuestion) {
        int total = questions.size();
        int correctCount = 0;
        List<QuestionOutcome> outcomes = new ArrayList<>(total);

        for (Question question : questions) {
            Long selectedId = selectedOptionByQuestion.get(question.getId());
            boolean correct = isSelectionCorrect(question, selectedId);
            if (correct) {
                correctCount++;
            }
            outcomes.add(new QuestionOutcome(question.getId(), selectedId, correct));
        }

        int scorePercent = total == 0 ? 0 : (int) Math.round(correctCount * 100.0 / total);
        boolean passed = total > 0 && scorePercent >= test.getPassMarkPercent();
        return new Result(scorePercent, passed, outcomes);
    }

    private static boolean isSelectionCorrect(Question question, Long selectedOptionId) {
        if (selectedOptionId == null) {
            return false;
        }
        for (QuestionOption option : question.getOptions()) {
            if (option.getId().equals(selectedOptionId)) {
                return option.isCorrect();
            }
        }
        // Selected option does not belong to this question -> incorrect.
        return false;
    }
}
