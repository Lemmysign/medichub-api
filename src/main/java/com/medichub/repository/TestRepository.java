package com.medichub.repository;

import com.medichub.model.Test;
import com.medichub.model.enums.TestKind;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TestRepository extends JpaRepository<Test, Long> {

    // --- Course tests ---
    List<Test> findByCourseIdOrderByCreatedAtAsc(Long courseId);

    Optional<Test> findByIdAndCourseId(Long id, Long courseId);

    long countByCourseInstructorId(Long instructorId);

    // --- Standalone exams (course is null): MCQs and Recalls ---
    Optional<Test> findByIdAndCourseIsNull(Long id);

    // --- Creator-side management (admin → all, instructor → own), filtered by kind + optional subject/year ---

    @Query("""
            select t from Test t
            where t.course is null and t.kind = :kind
              and (:ownerId is null or t.owner.id = :ownerId)
              and (:subjectId is null or t.subject.id = :subjectId)
              and (:examYear is null or t.examYear = :examYear)
            order by t.createdAt desc
            """)
    Page<Test> findManageable(@Param("kind") TestKind kind,
                              @Param("ownerId") Long ownerId,
                              @Param("subjectId") Long subjectId,
                              @Param("examYear") Integer examYear,
                              Pageable pageable);

    // --- Student-side (published only), filtered by kind + optional subject/year ---

    @Query("""
            select t from Test t
            where t.course is null and t.kind = :kind and t.published = true
              and (:subjectId is null or t.subject.id = :subjectId)
              and (:examYear is null or t.examYear = :examYear)
            order by t.createdAt desc
            """)
    Page<Test> findAvailable(@Param("kind") TestKind kind,
                             @Param("subjectId") Long subjectId,
                             @Param("examYear") Integer examYear,
                             Pageable pageable);

    /** Distinct sitting years across published Recalls, newest first — for the student year filter. */
    @Query("""
            select distinct t.examYear from Test t
            where t.course is null and t.kind = com.medichub.model.enums.TestKind.RECALL
              and t.published = true and t.examYear is not null
            order by t.examYear desc
            """)
    List<Integer> findPublishedRecallYears();
}
