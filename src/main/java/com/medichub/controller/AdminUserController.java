package com.medichub.controller;

import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.UserResponse;
import com.medichub.model.enums.Role;
import com.medichub.service.AdminUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public PagedResponse<UserResponse> list(@RequestParam(required = false) Role role,
                                            @RequestParam(required = false) Boolean enabled,
                                            @RequestParam(required = false) String q,
                                            @PageableDefault(size = 20) Pageable pageable) {
        return adminUserService.listUsers(role, enabled, q, pageable);
    }

    @PatchMapping("/{userId}/enabled")
    public UserResponse setEnabled(@PathVariable Long userId, @RequestParam boolean enabled) {
        return adminUserService.setEnabled(userId, enabled);
    }
}
