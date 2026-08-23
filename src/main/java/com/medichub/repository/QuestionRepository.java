package com.medichub.repository;

import com.medichub.model.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** Questions with their options in one query (single collection fetch — no MultipleBagFetch). */
    @EntityGraph(attributePaths = "options")
    List<Question> findByTestIdOrderByOrderIndexAsc(Long testId);

    Optional<Question> findByIdAndTestId(Long id, Long testId);

    long countByTestId(Long testId);

    @Query("select coalesce(max(q.orderIndex), -1) from Question q where q.test.id = :testId")
    int findMaxOrderIndex(@Param("testId") Long testId);
}
