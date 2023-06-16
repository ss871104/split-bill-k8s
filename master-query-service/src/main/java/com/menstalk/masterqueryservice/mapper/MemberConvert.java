package com.menstalk.masterqueryservice.mapper;

import com.menstalk.masterqueryservice.dto.MemberResponse;
import com.menstalk.masterqueryservice.models.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MemberConvert {
    MemberConvert INSTANCE = Mappers.getMapper(MemberConvert.class);

    @Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    MemberResponse memberConvertToMemberResponse(Member member);
}
