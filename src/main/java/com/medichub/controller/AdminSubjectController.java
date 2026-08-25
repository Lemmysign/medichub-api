package com.medichub.controller;

import com.medichub.dto.request.CreateSubjectRequest;
import com.medichub.dto.request.UpdateSubjectRequest;
import com.medichub.dto.response.SubjectResponse;
import com.medichub.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Admin management of taxonomy subjects (create / rename / reorder / deactivate). */
@RestController
@RequestMapping("/api/admin/subjects")
public class AdminSubjectController {

    private final SubjectService subjectService;

    public AdminSubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    /** All subjects, including inactive ones, for management. */
    @GetMapping
    public List<SubjectResponse> list() {
        return subjectService.listAll();
    }

    @PostMapping
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @PutMapping("/{id}")
    public SubjectResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSubjectRequest request) {
        return subjectService.update(id, request);
    }

    /** Soft delete — deactivates so existing tagged MCQs/Recalls keep their subject. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        subjectService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
