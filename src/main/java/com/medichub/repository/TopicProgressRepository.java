package com.medichub.repository;

import com.medichub.model.TopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicProgressRepository extends JpaRepository<TopicProgress, Long> {

    Optional<TopicProgress> findByStudentIdAndTopicId(Long studentId, Long topicId);

    @Query("""
            select count(tp) from TopicProgress tp
            where tp.student.id = :studentId and tp.topic.course.id = :courseId and tp.completed = true
            """)
    long countCompletedByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /** Completed-topic counts per course for one student, in a single grouped query. */
    @Query("""
            select tp.topic.course.id, count(tp) from TopicProgress tp
            where tp.student.id = :studentId and tp.topic.course.id in :courseIds and tp.completed = true
            group by tp.topic.course.id
            """)
    List<Object[]> countCompletedByStudentAndCourses(@Param("studentId") Long studentId,
                                                      @Param("courseIds") List<Long> courseIds);

    long countByStudentIdAndCompletedTrue(Long studentId);
}
