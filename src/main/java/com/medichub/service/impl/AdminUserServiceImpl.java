package com.medichub.service.impl;

import com.medichub.dto.response.PagedResponse;
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
    public PagedResponse<UserResponse> listUsers(Role role, String query, Pageable pageable) {
        String q = (query == null || query.isBlank()) ? "" : query.trim();
        return PagedResponse.from(userRepository.searchUsers(role, q, pageable), userMapper::toResponse);
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
}
