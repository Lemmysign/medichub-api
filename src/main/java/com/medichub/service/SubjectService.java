package com.medichub.service;

import com.medichub.dto.request.CreateSubjectRequest;
import com.medichub.dto.request.UpdateSubjectRequest;
import com.medichub.dto.response.SubjectResponse;

import java.util.List;

/** Manages the taxonomy subjects used to categorise MCQs and Recalls. */
public interface SubjectService {

    /** Active subjects only — for authoring pickers and student filters. */
    List<SubjectResponse> listActive();

    /** All subjects (active + inactive) — for admin management. */
    List<SubjectResponse> listAll();

    SubjectResponse create(CreateSubjectRequest request);

    SubjectResponse update(Long id, UpdateSubjectRequest request);

    /** Deactivates the subject (soft delete) so existing tagged content is preserved. */
    void deactivate(Long id);
}
