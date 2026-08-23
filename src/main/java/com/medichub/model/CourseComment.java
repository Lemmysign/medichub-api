package com.medichub.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Q&A / discussion. Self-referential: a student's question is a root comment,
 * the instructor's reply is a child (CLAUDE.md §4).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "course_comments", indexes = {
        @Index(name = "idx_comments_course", columnList = "course_id"),
        @Index(name = "idx_comments_parent", columnList = "parent_id")
})
public class CourseComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Optional: a comment may be scoped to a specific topic. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Denormalized author name for readable rows (kept in sync on rename). */
    @Column(name = "author_name")
    private String authorName;

    /** Null for a root question; set to the parent for a reply. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CourseComment parent;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CourseComment> replies = new ArrayList<>();
}
