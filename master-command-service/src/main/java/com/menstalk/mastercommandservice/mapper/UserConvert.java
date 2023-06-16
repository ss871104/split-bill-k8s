package com.menstalk.mastercommandservice.mapper;

import com.menstalk.mastercommandservice.dto.UserResponse;
import com.menstalk.mastercommandservice.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    @Mapping(target = "registerTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    UserResponse userConvertToUserResponse(User user);

    @Mapping(target = "registerTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    User userResponseConvertToUser(UserResponse userResponse);

}
