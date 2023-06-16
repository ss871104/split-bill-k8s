package com.menstalk.authservice.mapper;

import com.menstalk.authservice.dto.AuthResponse;
import com.menstalk.authservice.models.Auth;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuthConvert {
    AuthConvert INSTANCE = Mappers.getMapper(AuthConvert.class);

    AuthResponse authConvertToAuthResponse(Auth auth);

}
