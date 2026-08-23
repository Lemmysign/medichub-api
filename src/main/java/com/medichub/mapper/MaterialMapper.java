package com.medichub.mapper;

import com.medichub.dto.response.MaterialResponse;
import com.medichub.model.CourseMaterial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaterialMapper {

    @Mapping(target = "topicId", source = "topic.id")
    MaterialResponse toResponse(CourseMaterial material);
}
