package com.medichub.mapper;

import com.medichub.dto.response.CoursePreviewResponse;
import com.medichub.dto.response.CourseResponse;
import com.medichub.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TopicMapper.class)
public interface CourseMapper {

    @Mapping(target = "instructorId", source = "course.instructor.id")
    @Mapping(target = "instructorName", source = "course.instructor.fullName")
    @Mapping(target = "topicCount", source = "topicCount")
    CourseResponse toResponse(Course course, long topicCount);

    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "instructorName", source = "instructor.fullName")
    @Mapping(target = "topicCount", expression = "java((long) course.getTopics().size())")
    @Mapping(target = "topics", source = "topics")
    CoursePreviewResponse toPreview(Course course);
}
