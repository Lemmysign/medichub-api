package com.medichub.repository;

import com.medichub.model.CourseComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseCommentRepository extends JpaRepository<CourseComment, Long> {

    /** Root questions on a course, newest first. */
    @EntityGraph(attributePaths = {"author", "topic"})
    Page<CourseComment> findByCourseIdAndParentIsNullOrderByCreatedAtDesc(Long courseId, Pageable pageable);

    /** Root questions across an instructor's courses. */
    @EntityGraph(attributePaths = {"author", "topic", "course"})
    Page<CourseComment> findByCourseInstructorIdAndParentIsNullOrderByCreatedAtDesc(Long instructorId, Pageable pageable);

    /** Unanswered root questions across an instructor's courses (no replies yet). */
    @EntityGraph(attributePaths = {"author", "topic", "course"})
    @Query("""
            select c from CourseComment c
            where c.course.instructor.id = :instructorId and c.parent is null
              and not exists (select 1 from CourseComment r where r.parent = c)
            order by c.createdAt desc
            """)
    Page<CourseComment> findUnansweredRootsByInstructor(@Param("instructorId") Long instructorId, Pageable pageable);

    /** All replies for a set of root comments, oldest first. */
    @EntityGraph(attributePaths = {"author"})
    List<CourseComment> findByParentIdInOrderByCreatedAtAsc(List<Long> parentIds);

    @Modifying
    @Query("update CourseComment c set c.authorName = :name where c.author.id = :authorId")
    int updateAuthorName(@Param("authorId") Long authorId, @Param("name") String name);
}
