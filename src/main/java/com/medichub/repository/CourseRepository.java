package com.medichub.repository;

import com.medichub.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = "instructor")
    Page<Course> findByPublishedTrue(Pageable pageable);

    @EntityGraph(attributePaths = "instructor")
    Page<Course> findByInstructorId(Long instructorId, Pageable pageable);

    Optional<Course> findByIdAndPublishedTrue(Long id);

    /** Course preview with its topics eagerly loaded in one query (avoids N+1). */
    @EntityGraph(attributePaths = "topics")
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findWithTopicsById(@Param("id") Long id);

    long countByInstructorId(Long instructorId);

    /**
     * Topic counts for a set of courses in a single grouped query — used to enrich
     * paged course lists without triggering a lazy load per row.
     */
    @Query("select t.course.id, count(t) from Topic t where t.course.id in :courseIds group by t.course.id")
    List<Object[]> countTopicsByCourseIds(@Param("courseIds") List<Long> courseIds);

    /** Keep the denormalized instructor name in sync when the instructor renames. */
    @Modifying
    @Query("update Course c set c.instructorName = :name where c.instructor.id = :instructorId")
    int updateInstructorName(@Param("instructorId") Long instructorId, @Param("name") String name);
}
