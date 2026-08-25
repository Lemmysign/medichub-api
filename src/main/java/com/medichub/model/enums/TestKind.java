package com.medichub.model.enums;

/**
 * Distinguishes the two flavours of a <em>standalone</em> {@link com.medichub.model.Test}
 * (one with {@code course == null}). Course tests carry {@code MCQ} by default but are
 * always identified by their non-null course, never by this field.
 *
 * <ul>
 *   <li>{@code MCQ} — a subject-tagged practice exam (formerly "mock exam").</li>
 *   <li>{@code RECALL} — past questions from a real sitting, tagged with a subject and an exam year.</li>
 * </ul>
 */
public enum TestKind {
    MCQ,
    RECALL
}
