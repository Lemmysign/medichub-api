package com.medichub.service;

import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.PendingInstructorResponse;
import com.medichub.dto.response.UserResponse;
import com.medichub.model.enums.Role;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    PagedResponse<UserResponse> listUsers(Role role, Boolean enabled, String query, Pageable pageable);

    /** Enable or disable a student/instructor account. Disabling also revokes refresh tokens. */
    UserResponse setEnabled(Long userId, boolean enabled);

    /** Instructors who have signed up but are not yet approved (and not rejected). */
    PagedResponse<PendingInstructorResponse> listPendingInstructors(Pageable pageable);

    /** Approve a pending instructor so they can log in. */
    UserResponse approveInstructor(Long userId);

    /** Reject a pending instructor: disables the account (they cannot log in). */
    UserResponse rejectInstructor(Long userId);
}
