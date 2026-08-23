package com.medichub.mapper;

import com.medichub.dto.response.TopicPreviewResponse;
import com.medichub.dto.response.TopicResponse;
import com.medichub.model.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicMapper {

    @Mapping(target = "hasVideo", expression = "java(topic.getBunnyVideoId() != null)")
    TopicResponse toResponse(Topic topic);

    @Mapping(target = "hasVideo", expression = "java(topic.getBunnyVideoId() != null)")
    TopicPreviewResponse toPreview(Topic topic);

    List<TopicPreviewResponse> toPreviewList(List<Topic> topics);
}
