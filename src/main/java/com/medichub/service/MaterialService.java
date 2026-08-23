package com.medichub.service;

import com.medichub.dto.response.DownloadUrlResponse;
import com.medichub.dto.response.MaterialResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MaterialService {

    // Instructor (ownership-checked)
    MaterialResponse upload(Long courseId, Long topicId, MultipartFile file);

    List<MaterialResponse> listForInstructor(Long courseId);

    void delete(Long courseId, Long materialId);

    // Student (subscription-gated)
    List<MaterialResponse> listForStudent(Long courseId);

    DownloadUrlResponse getDownloadUrl(Long courseId, Long materialId);
}
