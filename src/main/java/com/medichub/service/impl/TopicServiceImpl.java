package com.medichub.service.impl;

import com.medichub.dto.request.CreateTopicRequest;
import com.medichub.dto.request.ReorderTopicsRequest;
import com.medichub.dto.request.UpdateTopicRequest;
import com.medichub.dto.response.TopicResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.mapper.TopicMapper;
import com.medichub.model.Course;
import com.medichub.model.Topic;
import com.medichub.repository.TopicRepository;
import com.medichub.security.SecurityUtils;
import com.medichub.service.CourseService;
import com.medichub.service.TopicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final CourseService courseService;
    private final TopicMapper topicMapper;

    public TopicServiceImpl(TopicRepository topicRepository,
                            CourseService courseService,
                            TopicMapper topicMapper) {
        this.topicRepository = topicRepository;
        this.courseService = courseService;
        this.topicMapper = topicMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicResponse> listTopics(Long courseId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        return topicRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(topicMapper::toResponse)
                .toList();
    }

    @Override
    public TopicResponse addTopic(Long courseId, CreateTopicRequest request) {
        Course course = courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Topic topic = new Topic();
        topic.setCourse(course);
        topic.setTitle(request.title());
        topic.setOrderIndex(topicRepository.findMaxOrderIndex(courseId) + 1);
        topic = topicRepository.save(topic);
        return topicMapper.toResponse(topic);
    }

    @Override
    public TopicResponse updateTopic(Long courseId, Long topicId, UpdateTopicRequest request) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Topic topic = requireTopic(topicId, courseId);
        topic.setTitle(request.title());
        return topicMapper.toResponse(topic);
    }

    @Override
    public void deleteTopic(Long courseId, Long topicId) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        Topic topic = requireTopic(topicId, courseId);
        topicRepository.delete(topic);
    }

    @Override
    public List<TopicResponse> reorderTopics(Long courseId, ReorderTopicsRequest request) {
        courseService.requireOwnedCourse(courseId, SecurityUtils.currentUserId());
        List<Topic> topics = topicRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        Set<Long> existingIds = new HashSet<>(topics.stream().map(Topic::getId).toList());
        Set<Long> requestedIds = new HashSet<>(request.topicIds());
        if (request.topicIds().size() != topics.size() || !existingIds.equals(requestedIds)) {
            throw new BadRequestException("Reorder list must contain exactly the course's topic ids");
        }

        // Assign each topic its new position from the requested ordering.
        for (int position = 0; position < request.topicIds().size(); position++) {
            Long topicId = request.topicIds().get(position);
            for (Topic topic : topics) {
                if (topic.getId().equals(topicId)) {
                    topic.setOrderIndex(position);
                    break;
                }
            }
        }
        topicRepository.saveAll(topics);

        return topics.stream()
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(topicMapper::toResponse)
                .toList();
    }

    private Topic requireTopic(Long topicId, Long courseId) {
        return topicRepository.findByIdAndCourseId(topicId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic", topicId));
    }
}
