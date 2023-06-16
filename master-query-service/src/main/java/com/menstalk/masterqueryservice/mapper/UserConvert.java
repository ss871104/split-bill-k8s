package com.menstalk.masterqueryservice.mapper;

import com.menstalk.masterqueryservice.dto.UserResponse;
import com.menstalk.masterqueryservice.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConvert {
    UserConvert INSTANCE = Mappers.getMapper(UserConvert.class);

    @Mapping(target = "registerTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    UserResponse userConvertToUserResponse(User user);

}
