package com.medichub.service;

import com.medichub.dto.request.CreateTopicRequest;
import com.medichub.dto.request.ReorderTopicsRequest;
import com.medichub.dto.request.UpdateTopicRequest;
import com.medichub.dto.response.TopicResponse;

import java.util.List;

public interface TopicService {

    List<TopicResponse> listTopics(Long courseId);

    TopicResponse addTopic(Long courseId, CreateTopicRequest request);

    TopicResponse updateTopic(Long courseId, Long topicId, UpdateTopicRequest request);

    void deleteTopic(Long courseId, Long topicId);

    List<TopicResponse> reorderTopics(Long courseId, ReorderTopicsRequest request);
}
