package com.medichub.model.enums;

/**
 * When correct answers + explanations are revealed to a student taking a test/mock.
 * <ul>
 *   <li>{@code IMMEDIATE} — after each answer; if wrong, the correct answer + explanation
 *       are shown before the student may continue (study mode).</li>
 *   <li>{@code ON_SUBMISSION} — nothing is revealed during the attempt; correct answers +
 *       explanations appear only in the results review after submitting (exam mode).</li>
 * </ul>
 */
public enum FeedbackMode {
    IMMEDIATE,
    ON_SUBMISSION
}
