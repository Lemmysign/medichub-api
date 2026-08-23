package com.medichub.service;

import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.UserResponse;
import com.medichub.model.enums.Role;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    PagedResponse<UserResponse> listUsers(Role role, String query, Pageable pageable);

    /** Enable or disable a student/instructor account. Disabling also revokes refresh tokens. */
    UserResponse setEnabled(Long userId, boolean enabled);
}
