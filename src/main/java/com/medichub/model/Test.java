package com.medichub.model;

import com.medichub.model.enums.FeedbackMode;
import com.medichub.model.enums.TestKind;
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
 *   <li><b>Standalone exam</b> — {@code course} is null; subscriber-only, timed
 *       ({@code durationMinutes}), visible when {@code published}; owned by its
 *       {@code owner} (instructor or admin). Its {@link #kind} is either {@code MCQ}
 *       (subject-tagged practice) or {@code RECALL} (past questions tagged with a subject
 *       and {@link #examYear}). See CLAUDE.md §5.</li>
 * </ul>
 * A test is standalone iff {@code course == null}; use {@link #getKind()} to tell MCQs from Recalls.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tests", indexes = {
        @Index(name = "idx_tests_course", columnList = "course_id"),
        @Index(name = "idx_tests_owner", columnList = "owner_id"),
        @Index(name = "idx_tests_kind_subject", columnList = "kind, subject_id")
})
public class Test extends BaseEntity {

    /** Null for standalone exams (MCQs and Recalls). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /** Creator of a standalone exam (instructor or admin); null for course tests. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    /**
     * MCQ vs RECALL for standalone exams. Course tests carry MCQ by default but are
     * identified by their non-null course, never by this field. Column default keeps
     * pre-existing rows valid when the schema is upgraded.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20, columnDefinition = "varchar(20) default 'MCQ'")
    private TestKind kind = TestKind.MCQ;

    /** Taxonomy subject for standalone exams (both MCQs and Recalls); null for course tests. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    /** The sitting year a Recall's questions came from; null for MCQs and course tests. */
    @Column(name = "exam_year")
    private Integer examYear;

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

    /** True for any standalone exam (MCQ or Recall) — i.e. not attached to a course. */
    public boolean isStandalone() {
        return course == null;
    }

    /** @deprecated use {@link #isStandalone()}; retained for existing callers. */
    @Deprecated
    public boolean isMockExam() {
        return course == null;
    }

    public boolean isRecall() {
        return course == null && kind == TestKind.RECALL;
    }
}
