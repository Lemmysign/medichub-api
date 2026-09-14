package com.medichub.controller;

import com.medichub.dto.response.DownloadUrlResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudyMaterialResponse;
import com.medichub.service.StudyService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Student-side Study module: browse published documents and open a short-lived view URL (gated). */
@RestController
@RequestMapping("/api/student/study")
public class StudentStudyController {

    private final StudyService studyService;

    public StudentStudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @GetMapping
    public PagedResponse<StudyMaterialResponse> available(@RequestParam(required = false) Long subjectId,
                                                          @PageableDefault(size = 12) Pageable pageable) {
        return studyService.listForStudent(subjectId, pageable);
    }

    @GetMapping("/{id}/view")
    public DownloadUrlResponse view(@PathVariable Long id) {
        return studyService.studentViewUrl(id);
    }
}
