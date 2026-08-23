package com.medichub.mapper;

import com.medichub.dto.response.UserResponse;
import com.medichub.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
