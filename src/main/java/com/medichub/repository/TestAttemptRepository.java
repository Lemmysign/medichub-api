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

    @EntityGraph(attributePaths = {"answers", "answers.question", "answers.selectedOption"})
    Optional<TestAttempt> findWithAnswersByIdAndStudentId(Long id, Long studentId);

    long countByStudentId(Long studentId);

    @Query("select coalesce(avg(a.scorePercent), 0) from TestAttempt a where a.student.id = :studentId")
    double averageScoreByStudent(@Param("studentId") Long studentId);

    /** Distinct students who have attempted any test on a given instructor's courses (dashboard). */
    @Query("select count(distinct a.student.id) from TestAttempt a where a.test.course.instructor.id = :instructorId")
    long countDistinctStudentsByInstructor(@Param("instructorId") Long instructorId);

    @Modifying
    @Query("update TestAttempt a set a.studentName = :name where a.student.id = :studentId")
    int updateStudentName(@Param("studentId") Long studentId, @Param("name") String name);
}
