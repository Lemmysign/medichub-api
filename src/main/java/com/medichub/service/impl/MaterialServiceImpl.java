package com.medichub.service.impl;

import com.medichub.dto.response.DownloadUrlResponse;
import com.medichub.dto.response.MaterialResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.MaterialMapper;
import com.medichub.model.Course;
import com.medichub.model.CourseMaterial;
import com.medichub.model.Topic;
import com.medichub.repository.CourseMaterialRepository;
import com.medichub.repository.TopicRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CourseService;
import com.medichub.service.MaterialService;
import com.medichub.service.StorageService;
import com.medichub.service.SubscriptionAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MaterialServiceImpl implements MaterialService {

    private static final Duration DOWNLOAD_TTL = Duration.ofMinutes(10);

    private final CourseMaterialRepository materialRepository;
    private final TopicRepository topicRepository;
    private final CourseService courseService;
    private final StorageService storageService;
    private final SubscriptionAccessService subscriptionAccessService;
    private final MaterialMapper materialMapper;

    public MaterialServiceImpl(CourseMaterialRepository materialRepository,
                               TopicRepository topicRepository,
                               CourseService courseService,
                               StorageService storageService,
                               SubscriptionAccessService subscriptionAccessService,
                               MaterialMapper materialMapper) {
        this.materialRepository = materialRepository;
        this.topicRepository = topicRepository;
        this.courseService = courseService;
        this.storageService = storageService;
        this.subscriptionAccessService = subscriptionAccessService;
        this.materialMapper = materialMapper;
    }

    @Override
    public MaterialResponse upload(Long courseId, Long topicId, MultipartFile file) {
        Course course = courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        Topic topic = null;
        if (topicId != null) {
            topic = topicRepository.findByIdAndCourseId(topicId, courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic", topicId));
        }

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String key = "materials/%d/%s-%s".formatted(courseId, UUID.randomUUID(),
                originalName.replaceAll("\\s+", "_"));

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Could not read uploaded file");
        }
        storageService.upload(key, bytes, file.getContentType());

        CourseMaterial material = new CourseMaterial();
        material.setCourse(course);
        material.setTopic(topic);
        material.setFileName(originalName);
        material.setContentType(file.getContentType());
        material.setR2Key(key);
        material.setSizeBytes(file.getSize());
        material = materialRepository.save(material);
        return materialMapper.toResponse(material);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialResponse> listForInstructor(Long courseId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        return materialRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(materialMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long courseId, Long materialId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        CourseMaterial material = requireMaterial(materialId, courseId);
        storageService.delete(material.getR2Key());
        materialRepository.delete(material);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialResponse> listForStudent(Long courseId) {
        courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        return materialRepository.findByCourseIdOrderByCreatedAtDesc(courseId).stream()
                .map(materialMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrlResponse getDownloadUrl(Long courseId, Long materialId) {
        courseService.requirePublishedCourse(courseId);
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        CourseMaterial material = requireMaterial(materialId, courseId);
        String url = storageService.presignedGetUrl(material.getR2Key(), DOWNLOAD_TTL);
        return new DownloadUrlResponse(url, DOWNLOAD_TTL.toSeconds());
    }

    private CourseMaterial requireMaterial(Long materialId, Long courseId) {
        return materialRepository.findByIdAndCourseId(materialId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Material", materialId));
    }
}
