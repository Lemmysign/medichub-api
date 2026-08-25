package com.medichub.service;

import com.medichub.model.Question;
import com.medichub.model.QuestionOption;
import com.medichub.model.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure auto-grading logic (no persistence), so it is fully unit-testable.
 *
 * <p>Set-based grading: a question is correct iff the set of options the student selected
 * (restricted to that question's options) exactly equals the set of options flagged correct.
 * This handles SINGLE_CHOICE / TRUE_FALSE (one correct) and MULTIPLE_CHOICE (one or more
 * correct) uniformly — all-or-nothing, no partial credit.
 */
public final class TestGrader {

    private TestGrader() {
    }

    public record QuestionOutcome(Long questionId, Set<Long> selectedOptionIds, boolean correct) {
    }

    public record Result(int scorePercent, boolean passed, List<QuestionOutcome> outcomes) {
    }

    public static Result grade(Test test, List<Question> questions, Map<Long, ? extends Collection<Long>> selectedByQuestion) {
        int total = questions.size();
        int correctCount = 0;
        List<QuestionOutcome> outcomes = new ArrayList<>(total);

        for (Question question : questions) {
            Collection<Long> selected = selectedByQuestion.get(question.getId());
            Set<Long> validSelected = validSelected(question, selected);
            boolean correct = isSelectionCorrect(question, selected);
            if (correct) {
                correctCount++;
            }
            outcomes.add(new QuestionOutcome(question.getId(), validSelected, correct));
        }

        int scorePercent = total == 0 ? 0 : (int) Math.round(correctCount * 100.0 / total);
        boolean passed = total > 0 && scorePercent >= test.getPassMarkPercent();
        return new Result(scorePercent, passed, outcomes);
    }

    /** True iff the selected options exactly match the question's correct-option set (non-empty). */
    public static boolean isSelectionCorrect(Question question, Collection<Long> selectedOptionIds) {
        Set<Long> correct = correctOptionIds(question);
        if (correct.isEmpty()) {
            return false;
        }
        return validSelected(question, selectedOptionIds).equals(correct);
    }

    private static Set<Long> correctOptionIds(Question question) {
        Set<Long> ids = new LinkedHashSet<>();
        for (QuestionOption o : question.getOptions()) {
            if (o.isCorrect()) {
                ids.add(o.getId());
            }
        }
        return ids;
    }

    /** The selected ids that actually belong to this question (guards against stray/foreign ids). */
    private static Set<Long> validSelected(Question question, Collection<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            return Set.of();
        }
        Set<Long> ownIds = new HashSet<>();
        for (QuestionOption o : question.getOptions()) {
            ownIds.add(o.getId());
        }
        Set<Long> out = new LinkedHashSet<>();
        for (Long id : selectedOptionIds) {
            if (id != null && ownIds.contains(id)) {
                out.add(id);
            }
        }
        return out;
    }
}
