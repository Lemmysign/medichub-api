package com.medichub.service.impl;

import com.medichub.dto.response.InstructorDashboardResponse;
import com.medichub.dto.response.StudentDashboardResponse;
import com.medichub.repository.CourseRepository;
import com.medichub.repository.EnrollmentRepository;
import com.medichub.repository.TestAttemptRepository;
import com.medichub.repository.TestRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TestRepository testRepository;
    private final TestAttemptRepository testAttemptRepository;

    public DashboardServiceImpl(CourseRepository courseRepository,
                                EnrollmentRepository enrollmentRepository,
                                TestRepository testRepository,
                                TestAttemptRepository testAttemptRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.testRepository = testRepository;
        this.testAttemptRepository = testAttemptRepository;
    }

    @Override
    public InstructorDashboardResponse getInstructorDashboard() {
        Long instructorId = SecurityUtils.currentUserId();
        return new InstructorDashboardResponse(
                courseRepository.countByInstructorId(instructorId),
                enrollmentRepository.countDistinctStudentsByInstructor(instructorId),
                testRepository.countByCourseInstructorId(instructorId),
                testAttemptRepository.countDistinctStudentsByInstructor(instructorId));
    }

    @Override
    public StudentDashboardResponse getStudentDashboard() {
        Long studentId = SecurityUtils.currentUserId();
        return new StudentDashboardResponse(
                enrollmentRepository.countByStudentId(studentId),
                testAttemptRepository.countByStudentId(studentId),
                (int) Math.round(testAttemptRepository.averageScoreByStudent(studentId)),
                enrollmentRepository.countCompletedCoursesByStudent(studentId));
    }
}
