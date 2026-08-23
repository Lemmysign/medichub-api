package com.medichub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Per student, per topic. Course progress % = completed topics / total topics.
 * Unique on (student, topic) (CLAUDE.md §4, §6).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "topic_progress",
        uniqueConstraints = @UniqueConstraint(name = "uk_progress_student_topic", columnNames = {"student_id", "topic_id"}),
        indexes = @Index(name = "idx_topic_progress_student", columnList = "student_id"))
public class TopicProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private int secondsWatched = 0;

    private Instant completedAt;
}
