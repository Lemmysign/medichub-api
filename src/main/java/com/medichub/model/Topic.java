package com.medichub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An ordered unit within a course. One video per topic (CLAUDE.md §4).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "topics", indexes = @Index(name = "idx_topics_course", columnList = "course_id"))
public class Topic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int orderIndex;

    /** Bunny Stream video GUID; null until the video is uploaded. */
    private String bunnyVideoId;

    private Integer videoDurationSeconds;
}
