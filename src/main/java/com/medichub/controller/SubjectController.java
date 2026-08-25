package com.medichub.controller;

import com.medichub.dto.response.SubjectResponse;
import com.medichub.service.SubjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Shared read-only subject list for any authenticated user — instructors tag content with it,
 * students filter MCQs/Recalls by it. Returns active subjects only. Admin CRUD lives at
 * {@code /api/admin/subjects}.
 */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<SubjectResponse> list() {
        return subjectService.listActive();
    }
}
