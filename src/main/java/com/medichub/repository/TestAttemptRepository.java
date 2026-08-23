package com.medichub.repository;

import com.medichub.model.TestAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    Page<TestAttempt> findByStudentIdAndTestIdOrderBySubmittedAtDesc(Long studentId, Long testId, Pageable pageable);

    Page<TestAttempt> findByStudentIdOrderBySubmittedAtDesc(Long studentId, Pageable pageable);

    // --- Mock exams: server-anchored, timed attempts (in-progress = submittedAt is null) ---
    Optional<TestAttempt> findFirstByStudentIdAndTestIdAndSubmittedAtIsNull(Long studentId, Long testId);

    Optional<TestAttempt> findByIdAndStudentId(Long id, Long studentId);

    Page<TestAttempt> findByStudentIdAndTestIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(
            Long studentId, Long testId, Pageable pageable);

    /** [testId, attemptCount, bestScore] for a student across a set of tests (mock list enrichment). */
    @Query("""
            select a.test.id, count(a), coalesce(max(a.scorePercent), 0)
            from TestAttempt a
            where a.student.id = :studentId and a.submittedAt is not null and a.test.id in :testIds
            group by a.test.id
            """)
    java.util.List<Object[]> attemptStatsByStudentAndTests(@Param("studentId") Long studentId,
                                                           @Param("testIds") java.util.List<Long> testIds);

    @EntityGraph(attributePaths = {"answers", "answers.question", "answers.selectedOption"})
    Optional<TestAttempt> findWithAnswersByIdAndStudentId(Long id, Long studentId);

    /** Submitted attempts only — excludes in-progress mock attempts. */
    long countByStudentIdAndSubmittedAtIsNotNull(Long studentId);

    @Query("""
            select coalesce(avg(a.scorePercent), 0) from TestAttempt a
            where a.student.id = :studentId and a.submittedAt is not null
            """)
    double averageScoreByStudent(@Param("studentId") Long studentId);

    /** Distinct students who have submitted an attempt on a given instructor's courses (dashboard). */
    @Query("""
            select count(distinct a.student.id) from TestAttempt a
            where a.test.course.instructor.id = :instructorId and a.submittedAt is not null
            """)
    long countDistinctStudentsByInstructor(@Param("instructorId") Long instructorId);

    @Modifying
    @Query("update TestAttempt a set a.studentName = :name where a.student.id = :studentId")
    int updateStudentName(@Param("studentId") Long studentId, @Param("name") String name);
}
