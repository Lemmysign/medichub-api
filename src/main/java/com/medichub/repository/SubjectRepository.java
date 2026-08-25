package com.medichub.repository;

import com.medichub.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /** All subjects for admin management, ordered for display. */
    List<Subject> findAllByOrderByOrderIndexAscNameAsc();

    /** Active subjects only — for authoring pickers and student filters. */
    List<Subject> findByActiveTrueOrderByOrderIndexAscNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySlug(String slug);

    Optional<Subject> findBySlug(String slug);
}
