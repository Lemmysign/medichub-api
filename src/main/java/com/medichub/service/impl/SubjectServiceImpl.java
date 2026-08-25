package com.medichub.service.impl;

import com.medichub.dto.request.CreateSubjectRequest;
import com.medichub.dto.request.UpdateSubjectRequest;
import com.medichub.dto.response.SubjectResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.model.Subject;
import com.medichub.repository.SubjectRepository;
import com.medichub.service.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> listActive() {
        return subjectRepository.findByActiveTrueOrderByOrderIndexAscNameAsc().stream()
                .map(SubjectServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> listAll() {
        return subjectRepository.findAllByOrderByOrderIndexAscNameAsc().stream()
                .map(SubjectServiceImpl::toResponse).toList();
    }

    @Override
    public SubjectResponse create(CreateSubjectRequest request) {
        String name = request.name().trim();
        if (subjectRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("A subject named '" + name + "' already exists");
        }
        Subject subject = new Subject();
        subject.setName(name);
        subject.setSlug(uniqueSlug(name));
        subject.setOrderIndex((int) subjectRepository.count()); // append to the end
        subject.setActive(true);
        return toResponse(subjectRepository.save(subject));
    }

    @Override
    public SubjectResponse update(Long id, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
        String name = request.name().trim();
        if (!subject.getName().equalsIgnoreCase(name) && subjectRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("A subject named '" + name + "' already exists");
        }
        if (!subject.getName().equalsIgnoreCase(name)) {
            subject.setSlug(uniqueSlug(name));
        }
        subject.setName(name);
        subject.setOrderIndex(request.orderIndex());
        subject.setActive(request.active());
        return toResponse(subject);
    }

    @Override
    public void deactivate(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
        subject.setActive(false);
    }

    // ----------------------------------------------------------------------

    private String uniqueSlug(String name) {
        String base = slugify(name);
        String candidate = base;
        int n = 2;
        while (subjectRepository.existsBySlug(candidate)) {
            candidate = base + "-" + n++;
        }
        return candidate;
    }

    private static String slugify(String name) {
        String slug = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "subject" : slug;
    }

    private static SubjectResponse toResponse(Subject s) {
        return new SubjectResponse(s.getId(), s.getName(), s.getSlug(), s.getOrderIndex(), s.isActive());
    }
}
