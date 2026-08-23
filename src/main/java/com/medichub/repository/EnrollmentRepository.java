package com.medichub.repository;

import com.medichub.model.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    @EntityGraph(attributePaths = {"course", "course.instructor"})
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    long countByStudentId(Long studentId);

    /** Distinct students enrolled across a given instructor's courses (instructor dashboard). */
    @Query("select count(distinct e.student.id) from Enrollment e where e.course.instructor.id = :instructorId")
    long countDistinctStudentsByInstructor(@Param("instructorId") Long instructorId);

    /** Enrolled courses the student has fully completed (all topics done). */
    @Query("""
            select count(e) from Enrollment e
            where e.student.id = :studentId
              and (select count(t) from Topic t where t.course = e.course) > 0
              and (select count(t) from Topic t where t.course = e.course)
                  = (select count(tp) from TopicProgress tp
                     where tp.student.id = :studentId and tp.topic.course = e.course and tp.completed = true)
            """)
    long countCompletedCoursesByStudent(@Param("studentId") Long studentId);

    @Modifying
    @Query("update Enrollment e set e.studentName = :name where e.student.id = :studentId")
    int updateStudentName(@Param("studentId") Long studentId, @Param("name") String name);
}
