package com.medichub.service;

import com.medichub.dto.request.ChangeEmailRequest;
import com.medichub.dto.request.ChangeNameRequest;
import com.medichub.dto.request.ChangePasswordRequest;
import com.medichub.dto.response.UserResponse;

/** Self-service account settings for the current authenticated user (all roles). */
public interface AccountService {

    UserResponse getCurrentUser();

    UserResponse changeName(ChangeNameRequest request);

    UserResponse changeEmail(ChangeEmailRequest request);

    void changePassword(ChangePasswordRequest request);
}
