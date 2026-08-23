package com.medichub.repository;

import com.medichub.model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {

    List<CourseMaterial> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    Optional<CourseMaterial> findByIdAndCourseId(Long id, Long courseId);
}
