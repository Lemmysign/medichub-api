package com.medichub.service.impl;

import com.medichub.dto.request.CreateCourseRequest;
import com.medichub.dto.request.UpdateCourseRequest;
import com.medichub.dto.response.CoursePreviewResponse;
import com.medichub.dto.response.CourseResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.exception.AccessDeniedException;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.CourseMapper;
import com.medichub.model.Course;
import com.medichub.model.User;
import com.medichub.repository.CourseRepository;
import com.medichub.repository.TopicRepository;
import com.medichub.repository.UserRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CourseService;
import com.medichub.service.StorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;
    private final StorageService storageService;

    public CourseServiceImpl(CourseRepository courseRepository,
                             TopicRepository topicRepository,
                             UserRepository userRepository,
                             CourseMapper courseMapper,
                             StorageService storageService) {
        this.courseRepository = courseRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.courseMapper = courseMapper;
        this.storageService = storageService;
    }

    @Override
    public CourseResponse create(CreateCourseRequest request) {
        User instructor = userRepository.findById(SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", SecurityUtils.currentUserId()));
        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setInstructor(instructor);
        course.setInstructorName(instructor.getFullName());
        course.setPublished(false);
        course = courseRepository.save(course);
        return courseMapper.toResponse(course, 0L);
    }

    @Override
    public CourseResponse uploadThumbnail(Long courseId, MultipartFile file) {
        Course course = requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Thumbnail file is required");
        }
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "thumbnail" : file.getOriginalFilename());
        String key = "thumbnails/%d/%s-%s".formatted(courseId, UUID.randomUUID(),
                originalName.replaceAll("\\s+", "_"));
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Could not read uploaded file");
        }
        storageService.upload(key, bytes, file.getContentType());
        course.setThumbnailUrl(storageService.publicUrl(key));
        return courseMapper.toResponse(course, topicRepository.countByCourseId(courseId));
    }

    @Override
    public CourseResponse update(Long courseId, UpdateCourseRequest request) {
        Course course = requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        course.setTitle(request.title());
        course.setDescription(request.description());
        return courseMapper.toResponse(course, topicRepository.countByCourseId(courseId));
    }

    @Override
    public void delete(Long courseId) {
        Course course = requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        courseRepository.delete(course);
    }

    @Override
    public CourseResponse setPublished(Long courseId, boolean published) {
        Course course = requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        course.setPublished(published);
        return courseMapper.toResponse(course, topicRepository.countByCourseId(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getOwnedCourse(Long courseId) {
        Course course = requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        return courseMapper.toResponse(course, topicRepository.countByCourseId(courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CourseResponse> listMyCourses(Pageable pageable) {
        return toPagedResponse(courseRepository.findByInstructorId(SecurityUtils.currentUserId(), pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CourseResponse> listPublished(Pageable pageable) {
        return toPagedResponse(courseRepository.findByPublishedTrue(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public CoursePreviewResponse getPublicPreview(Long courseId) {
        Course course = courseRepository.findWithTopicsById(courseId)
                .filter(Course::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return courseMapper.toPreview(course);
    }

    @Override
    @Transactional(readOnly = true)
    public Course requireOwnedCourse(Long courseId, Long instructorId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new AccessDeniedException("You do not own this course");
        }
        return course;
    }

    @Override
    @Transactional(readOnly = true)
    public Course requirePublishedCourse(Long courseId) {
        return courseRepository.findByIdAndPublishedTrue(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Course requireCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
    }

    // ----------------------------------------------------------------------

    /** Enrich a page of courses with topic counts in a single grouped query (no N+1). */
    private PagedResponse<CourseResponse> toPagedResponse(Page<Course> page) {
        List<Long> ids = page.getContent().stream().map(Course::getId).toList();
        Map<Long, Long> topicCounts = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : courseRepository.countTopicsByCourseIds(ids)) {
                topicCounts.put((Long) row[0], (Long) row[1]);
            }
        }
        return PagedResponse.from(page,
                course -> courseMapper.toResponse(course, topicCounts.getOrDefault(course.getId(), 0L)));
    }
}
