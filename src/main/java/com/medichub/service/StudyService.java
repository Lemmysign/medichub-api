package com.medichub.service;

import com.medichub.dto.response.DownloadUrlResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudyMaterialResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/** Study module: instructors upload PDFs/Word docs; students read them in-browser (gated). */
public interface StudyService {

    // --- creator (instructor/admin) ---
    StudyMaterialResponse upload(String title, String description, Long subjectId, MultipartFile file);

    /** Admin sees all; an instructor sees only their own. Optional subject filter. */
    PagedResponse<StudyMaterialResponse> listForCreator(Long subjectId, Pageable pageable);

    StudyMaterialResponse setPublished(Long id, boolean published);

    void delete(Long id);

    /** Signed view URL for the creator's own preview. */
    DownloadUrlResponse creatorViewUrl(Long id);

    // --- student ---
    PagedResponse<StudyMaterialResponse> listForStudent(Long subjectId, Pageable pageable);

    /** Signed view URL for a published document; subscription-gated. */
    DownloadUrlResponse studentViewUrl(Long id);
}
