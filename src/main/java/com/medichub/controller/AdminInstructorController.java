package com.medichub.controller;

import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.PendingInstructorResponse;
import com.medichub.dto.response.UserResponse;
import com.medichub.service.AdminUserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin: moderate instructor sign-ups (approve / reject those awaiting approval). */
@RestController
@RequestMapping("/api/admin/instructors")
public class AdminInstructorController {

    private final AdminUserService adminUserService;

    public AdminInstructorController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping("/pending")
    public PagedResponse<PendingInstructorResponse> pending(@PageableDefault(size = 20) Pageable pageable) {
        return adminUserService.listPendingInstructors(pageable);
    }

    @PostMapping("/{id}/approve")
    public UserResponse approve(@PathVariable Long id) {
        return adminUserService.approveInstructor(id);
    }

    @PostMapping("/{id}/reject")
    public UserResponse reject(@PathVariable Long id) {
        return adminUserService.rejectInstructor(id);
    }
}
