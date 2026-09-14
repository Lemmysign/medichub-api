package com.medichub.service.impl;

import com.medichub.dto.response.DownloadUrlResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.StudyMaterialResponse;
import com.medichub.exception.AccessDeniedException;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.model.StudyMaterial;
import com.medichub.model.Subject;
import com.medichub.model.User;
import com.medichub.repository.StudyMaterialRepository;
import com.medichub.repository.SubjectRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.DocumentStorageService;
import com.medichub.service.StudyService;
import com.medichub.service.SubscriptionAccessService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class StudyServiceImpl implements StudyService {

    private static final Duration VIEW_TTL = Duration.ofMinutes(30);
    private static final Set<String> ALLOWED = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final StudyMaterialRepository studyRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService documentStorage;
    private final SubscriptionAccessService subscriptionAccessService;

    public StudyServiceImpl(StudyMaterialRepository studyRepository,
                            SubjectRepository subjectRepository,
                            UserRepository userRepository,
                            DocumentStorageService documentStorage,
                            SubscriptionAccessService subscriptionAccessService) {
        this.studyRepository = studyRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.documentStorage = documentStorage;
        this.subscriptionAccessService = subscriptionAccessService;
    }

    @Override
    public StudyMaterialResponse upload(String title, String description, Long subjectId, MultipartFile file) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("Title is required");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("A PDF or Word file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType)) {
            throw new BadRequestException("Only PDF or Word (.doc/.docx) files are allowed");
        }

        User owner = userRepository.getReferenceById(SecurityUtils.currentUserId());
        Subject subject = subjectId == null ? null : subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        // public_id keeps the original filename (incl. extension) so the URL serves the right type.
        String publicId = "medichub/study/%s-%s".formatted(UUID.randomUUID(),
                originalName.replaceAll("\\s+", "_"));

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Could not read the uploaded file");
        }
        String url = documentStorage.uploadDocument(publicId, bytes);

        StudyMaterial material = new StudyMaterial();
        material.setOwner(owner);
        material.setSubject(subject);
        material.setTitle(title.trim());
        material.setDescription(description);
        material.setFileName(originalName);
        material.setContentType(contentType);
        material.setR2Key(publicId);
        material.setUrl(url);
        material.setSizeBytes(file.getSize());
        material.setPublished(false);
        return toResponse(studyRepository.save(material));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudyMaterialResponse> listForCreator(Long subjectId, Pageable pageable) {
        Long ownerId = SecurityUtils.isAdmin() ? null : SecurityUtils.currentUserId();
        return PagedResponse.from(studyRepository.findManageable(ownerId, subjectId, pageable), this::toResponse);
    }

    @Override
    public StudyMaterialResponse setPublished(Long id, boolean published) {
        StudyMaterial material = requireManageable(id);
        material.setPublished(published);
        return toResponse(material);
    }

    @Override
    public void delete(Long id) {
        StudyMaterial material = requireManageable(id);
        documentStorage.deleteDocument(material.getR2Key());
        studyRepository.delete(material);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrlResponse creatorViewUrl(Long id) {
        StudyMaterial material = requireManageable(id);
        return signed(material);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudyMaterialResponse> listForStudent(Long subjectId, Pageable pageable) {
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        return PagedResponse.from(studyRepository.findPublished(subjectId, pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrlResponse studentViewUrl(Long id) {
        subscriptionAccessService.requireActiveAccess(SecurityUtils.currentUserId());
        StudyMaterial material = studyRepository.findById(id)
                .filter(StudyMaterial::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("Study material", id));
        return signed(material);
    }

    // ----------------------------------------------------------------------

    private DownloadUrlResponse signed(StudyMaterial material) {
        // Cloudinary delivery URL is stable; TTL is nominal (kept for the response shape).
        return new DownloadUrlResponse(material.getUrl(), VIEW_TTL.toSeconds());
    }

    private StudyMaterial requireManageable(Long id) {
        StudyMaterial material = studyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Study material", id));
        if (SecurityUtils.isAdmin()) {
            return material;
        }
        if (material.getOwner() == null || !material.getOwner().getId().equals(SecurityUtils.currentUserId())) {
            throw new AccessDeniedException("You do not own this study material");
        }
        return material;
    }

    private StudyMaterialResponse toResponse(StudyMaterial s) {
        Subject subject = s.getSubject();
        return new StudyMaterialResponse(
                s.getId(), s.getTitle(), s.getDescription(),
                subject == null ? null : subject.getId(),
                subject == null ? null : subject.getName(),
                s.getFileName(), s.getContentType(), s.getSizeBytes(), s.isPublished(),
                s.getOwner() == null ? null : s.getOwner().getFullName(),
                s.getCreatedAt());
    }
}
