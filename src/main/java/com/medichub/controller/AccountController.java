package com.medichub.controller;

import com.medichub.dto.request.ChangeEmailRequest;
import com.medichub.dto.request.ChangeNameRequest;
import com.medichub.dto.request.ChangePasswordRequest;
import com.medichub.dto.response.UserResponse;
import com.medichub.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Account settings for the current authenticated user, any role (CLAUDE.md §5.1). */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public UserResponse me() {
        return accountService.getCurrentUser();
    }

    @PatchMapping("/name")
    public UserResponse changeName(@Valid @RequestBody ChangeNameRequest request) {
        return accountService.changeName(request);
    }

    @PatchMapping("/email")
    public UserResponse changeEmail(@Valid @RequestBody ChangeEmailRequest request) {
        return accountService.changeEmail(request);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
