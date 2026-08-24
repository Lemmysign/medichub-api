package com.medichub.model;

import com.medichub.model.enums.FeedbackMode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * An auto-graded MCQ test. Two flavours share this entity:
 * <ul>
 *   <li><b>Course test</b> — {@code course} set, {@code owner}/{@code published}/
 *       {@code durationMinutes} unused; ownership derives from the course instructor.</li>
 *   <li><b>Mock exam</b> — {@code course} is null; standalone, subscriber-only, timed
 *       ({@code durationMinutes}), visible when {@code published}; owned by its
 *       {@code owner} (instructor or admin). See CLAUDE.md §5.</li>
 * </ul>
 * A test is a mock exam iff {@code course == null}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tests", indexes = {
        @Index(name = "idx_tests_course", columnList = "course_id"),
        @Index(name = "idx_tests_owner", columnList = "owner_id")
})
public class Test extends BaseEntity {

    /** Null for standalone mock exams. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /** Creator of a standalone mock exam (instructor or admin); null for course tests. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private int passMarkPercent;

    /** Time limit for mock exams (minutes); null = untimed (course tests). */
    private Integer durationMinutes;

    /** Visibility for standalone mock exams; ignored for course tests. */
    @Column(nullable = false)
    private boolean published = false;

    /** When correct answers + explanations are revealed to the student. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackMode feedbackMode = FeedbackMode.ON_SUBMISSION;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<Question> questions = new ArrayList<>();

    public boolean isMockExam() {
        return course == null;
    }
}
