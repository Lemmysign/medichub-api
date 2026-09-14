package com.medichub.controller;

import com.medichub.dto.response.DownloadUrlResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudyMaterialResponse;
import com.medichub.service.StudyService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Creator-side Study document management — instructors and admins. */
@RestController
@RequestMapping("/api/study")
@PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StudyMaterialResponse> upload(@RequestParam("title") String title,
                                                        @RequestParam(value = "description", required = false) String description,
                                                        @RequestParam(value = "subjectId", required = false) Long subjectId,
                                                        @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studyService.upload(title, description, subjectId, file));
    }

    @GetMapping
    public PagedResponse<StudyMaterialResponse> list(@RequestParam(required = false) Long subjectId,
                                                     @PageableDefault(size = 20) Pageable pageable) {
        return studyService.listForCreator(subjectId, pageable);
    }

    @GetMapping("/{id}/view")
    public DownloadUrlResponse view(@PathVariable Long id) {
        return studyService.creatorViewUrl(id);
    }

    @PatchMapping("/{id}/publish")
    public StudyMaterialResponse setPublished(@PathVariable Long id, @RequestParam boolean published) {
        return studyService.setPublished(id, published);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
