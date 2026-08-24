package com.medichub.service;

import com.medichub.dto.request.CreateOptionRequest;
import com.medichub.exception.BadRequestException;
import com.medichub.model.Question;
import com.medichub.model.QuestionOption;
import com.medichub.model.enums.QuestionType;

import java.util.List;

/**
 * Shared question-authoring rules for both course tests and mock exams:
 * correct-answer validation (single/true-false = exactly one correct; multiple = at least
 * one) and option persistence.
 */
public final class QuestionAuthoring {

    private QuestionAuthoring() {
    }

    public static QuestionType resolveType(QuestionType requested) {
        return requested == null ? QuestionType.MULTIPLE_CHOICE : requested;
    }

    /** Enforces the correct-answer count rules for the given question type. */
    public static void validate(QuestionType type, List<CreateOptionRequest> options) {
        long correct = options.stream().filter(o -> Boolean.TRUE.equals(o.correct())).count();
        if (correct == 0) {
            throw new BadRequestException("A question must have at least one correct option");
        }
        if ((type == QuestionType.SINGLE_CHOICE || type == QuestionType.TRUE_FALSE) && correct > 1) {
            throw new BadRequestException(
                    "A " + (type == QuestionType.TRUE_FALSE ? "true/false" : "single-choice")
                            + " question must have exactly one correct option");
        }
    }

    /**
     * Updates a question's options <em>in place</em>: existing option rows are reused by position
     * (keeping their ids, so historical {@code attempt_answers} keep referencing unchanged options),
     * new options are appended, and surplus options are removed. Preferred over clear()+applyOptions
     * for edits, which would orphan-delete every option and break the FK from past attempts.
     */
    public static void reconcileOptions(Question question, List<CreateOptionRequest> options) {
        List<QuestionOption> existing = question.getOptions(); // ordered by orderIndex (see @OrderBy)
        for (int i = 0; i < options.size(); i++) {
            CreateOptionRequest req = options.get(i);
            boolean correct = Boolean.TRUE.equals(req.correct());
            if (i < existing.size()) {
                QuestionOption option = existing.get(i);
                option.setText(req.text());
                option.setCorrect(correct);
                option.setOrderIndex(i);
            } else {
                QuestionOption option = new QuestionOption();
                option.setQuestion(question);
                option.setText(req.text());
                option.setCorrect(correct);
                option.setOrderIndex(i);
                existing.add(option);
            }
        }
        if (existing.size() > options.size()) {
            // Drop the surplus (orphanRemoval). A removed option already referenced by an attempt is
            // handled by the ON DELETE SET NULL on attempt_answers.selected_option_id.
            existing.subList(options.size(), existing.size()).clear();
        }
    }

    /** Replaces the question's options with the supplied set (order preserved). */
    public static void applyOptions(Question question, List<CreateOptionRequest> options) {
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
