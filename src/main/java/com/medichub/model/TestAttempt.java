package com.medichub.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Kept forever so students can reference past attempts (CLAUDE.md §4, §5.3).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "test_attempts")
public class TestAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Denormalized student name for readable rows (kept in sync on rename). */
    @Column(name = "student_name")
    private String studentName;

    @Column(nullable = false)
    private int scorePercent;

    @Column(nullable = false)
    private boolean passed = false;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant submittedAt;

    /** Accumulated paused time (seconds) for timed mocks in immediate mode; extends the deadline. */
    @Column(nullable = false)
    private int pausedSeconds = 0;

    /** Set while an explanation card is open (timer paused); null when running. Server-measured. */
    private Instant pauseStartedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AttemptAnswer> answers = new ArrayList<>();
}
