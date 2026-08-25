package com.medichub.service.impl;

import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.PendingInstructorResponse;
import com.medichub.dto.response.UserResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.UserMapper;
import com.medichub.model.User;
import com.medichub.model.enums.Role;
import com.medichub.repository.RefreshTokenRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.DisabledUserRegistry;
import com.medichub.service.AdminUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper userMapper;
    private final DisabledUserRegistry disabledUserRegistry;

    public AdminUserServiceImpl(UserRepository userRepository,
                                RefreshTokenRepository refreshTokenRepository,
                                UserMapper userMapper,
                                DisabledUserRegistry disabledUserRegistry) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userMapper = userMapper;
        this.disabledUserRegistry = disabledUserRegistry;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> listUsers(Role role, Boolean enabled, String query, Pageable pageable) {
        String q = (query == null || query.isBlank()) ? "" : query.trim();
        return PagedResponse.from(userRepository.searchUsers(role, enabled, q, pageable), userMapper::toResponse);
    }

    @Override
    public UserResponse setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be enabled or disabled");
        }
        user.setEnabled(enabled);
        if (!enabled) {
            // Force logout: revoke refresh tokens AND reject live access tokens immediately.
            refreshTokenRepository.revokeAllForUser(user);
            disabledUserRegistry.markDisabled(userId);
        } else {
            disabledUserRegistry.markEnabled(userId);
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PendingInstructorResponse> listPendingInstructors(Pageable pageable) {
        return PagedResponse.from(
                userRepository.findByRoleAndApprovedFalseAndEnabledTrueOrderByCreatedAtAsc(Role.INSTRUCTOR, pageable),
                u -> new PendingInstructorResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                        u.isEmailVerified(), u.getCreatedAt()));
    }

    @Override
    public UserResponse approveInstructor(Long userId) {
        User instructor = requirePendingInstructor(userId);
        instructor.setApproved(true);
        return userMapper.toResponse(instructor);
    }

    @Override
    public UserResponse rejectInstructor(Long userId) {
        User instructor = requirePendingInstructor(userId);
        // Rejection = disable the account; the approval flag stays false.
        instructor.setEnabled(false);
        refreshTokenRepository.revokeAllForUser(instructor);
        disabledUserRegistry.markDisabled(userId);
        return userMapper.toResponse(instructor);
    }

    private User requirePendingInstructor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() != Role.INSTRUCTOR) {
            throw new BadRequestException("This account is not an instructor");
        }
        return user;
    }
}
