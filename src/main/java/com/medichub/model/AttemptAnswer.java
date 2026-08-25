package com.medichub.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private TestAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // First selected option, kept for single-choice history / older readers. If that option is later
    // removed, null this link rather than block the delete; the `correct` flag preserves the score.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private QuestionOption selectedOption;

    /**
     * All option ids the student selected for this question — supports multiple-choice
     * (multiple correct). Stored as raw ids (a historical record), not FKs, so later option
     * edits/deletes never cascade into past attempts.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "attempt_answer_options", joinColumns = @JoinColumn(name = "attempt_answer_id"))
    @Column(name = "option_id", nullable = false)
    private Set<Long> selectedOptionIds = new LinkedHashSet<>();

    @Column(nullable = false)
    private boolean correct = false;
}
