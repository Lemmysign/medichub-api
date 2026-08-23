package com.medichub.repository;

import com.medichub.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    Optional<Topic> findByIdAndCourseId(Long id, Long courseId);

    long countByCourseId(Long courseId);

    @Query("select coalesce(max(t.orderIndex), -1) from Topic t where t.course.id = :courseId")
    int findMaxOrderIndex(@Param("courseId") Long courseId);
}
