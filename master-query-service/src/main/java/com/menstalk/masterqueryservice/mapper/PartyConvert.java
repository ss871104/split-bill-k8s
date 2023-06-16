package com.menstalk.masterqueryservice.mapper;

import com.menstalk.masterqueryservice.dto.PartyResponse;
import com.menstalk.masterqueryservice.models.Party;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PartyConvert {
    PartyConvert INSTANCE = Mappers.getMapper(PartyConvert.class);

    @Mapping(target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    PartyResponse partyConvertToPartyResponse(Party party);
}
