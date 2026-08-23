package com.medichub.service.impl;

import com.medichub.dto.request.ChangeEmailRequest;
import com.medichub.dto.request.ChangeNameRequest;
import com.medichub.dto.request.ChangePasswordRequest;
import com.medichub.dto.response.UserResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.UserMapper;
import com.medichub.model.User;
import com.medichub.repository.CourseCommentRepository;
import com.medichub.repository.CourseRepository;
import com.medichub.repository.EnrollmentRepository;
import com.medichub.repository.PaymentRepository;
import com.medichub.repository.RefreshTokenRepository;
import com.medichub.repository.SubscriptionRepository;
import com.medichub.repository.TestAttemptRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.AccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    // Repositories holding denormalized name/email copies to keep in sync.
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final CourseCommentRepository courseCommentRepository;
    private final CourseRepository courseRepository;

    public AccountServiceImpl(UserRepository userRepository,
                              RefreshTokenRepository refreshTokenRepository,
                              PasswordEncoder passwordEncoder,
                              UserMapper userMapper,
                              SubscriptionRepository subscriptionRepository,
                              PaymentRepository paymentRepository,
                              EnrollmentRepository enrollmentRepository,
                              TestAttemptRepository testAttemptRepository,
                              CourseCommentRepository courseCommentRepository,
                              CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.testAttemptRepository = testAttemptRepository;
        this.courseCommentRepository = courseCommentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return userMapper.toResponse(currentUser());
    }

    @Override
    public UserResponse changeName(ChangeNameRequest request) {
        User user = currentUser();
        String newName = request.fullName().trim();
        user.setFullName(newName);
        propagateName(user.getId(), newName);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse changeEmail(ChangeEmailRequest request) {
        User user = currentUser();
        String newEmail = request.email().trim().toLowerCase();
        if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new BadRequestException("That email is already in use");
        }
        user.setEmail(newEmail);
        propagateEmail(user.getId(), newEmail);
        return userMapper.toResponse(user);
    }

    /** Keep denormalized name copies across the browsed tables in sync. */
    private void propagateName(Long userId, String name) {
        subscriptionRepository.updateStudentName(userId, name);
        paymentRepository.updateStudentName(userId, name);
        enrollmentRepository.updateStudentName(userId, name);
        testAttemptRepository.updateStudentName(userId, name);
        courseCommentRepository.updateAuthorName(userId, name);
        courseRepository.updateInstructorName(userId, name);
    }

    private void propagateEmail(Long userId, String email) {
        subscriptionRepository.updateStudentEmail(userId, email);
        paymentRepository.updateStudentEmail(userId, email);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (user.getPasswordHash() == null) {
            throw new BadRequestException("This account has no password set (Google sign-in)");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        // Force re-login everywhere after a password change.
        refreshTokenRepository.revokeAllForUser(user);
    }

    private User currentUser() {
        Long id = SecurityUtils.currentUserId();
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
