package com.medichub.repository;

import com.medichub.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestRepository extends JpaRepository<Test, Long> {

    List<Test> findByCourseIdOrderByCreatedAtAsc(Long courseId);

    Optional<Test> findByIdAndCourseId(Long id, Long courseId);

    long countByCourseInstructorId(Long instructorId);
}
