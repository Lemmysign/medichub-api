package com.medichub.repository;

import com.medichub.model.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRepository extends JpaRepository<Test, Long> {

    // --- Course tests ---
    List<Test> findByCourseIdOrderByCreatedAtAsc(Long courseId);

    Optional<Test> findByIdAndCourseId(Long id, Long courseId);

    long countByCourseInstructorId(Long instructorId);

    // --- Mock exams (course is null) ---
    Optional<Test> findByIdAndCourseIsNull(Long id);

    /** Published mocks for students. */
    Page<Test> findByCourseIsNullAndPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    /** All mocks (admin management). */
    Page<Test> findByCourseIsNullOrderByCreatedAtDesc(Pageable pageable);

    /** An instructor's own mocks. */
    Page<Test> findByCourseIsNullAndOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);
}
