package com.menstalk.billqueryservice.mapper;

import com.menstalk.billqueryservice.dto.BillDetailResponse;
import com.menstalk.billqueryservice.dto.BillResponse;
import com.menstalk.billqueryservice.models.Bill;
import com.menstalk.billqueryservice.models.BillDetail;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BillConvert {
    BillConvert INSTANCE = Mappers.getMapper(BillConvert.class);

    BillResponse billConvertToBillResponse(Bill bill);
    BillDetailResponse billDetailConvertToBillDetailResponse(BillDetail billDetail);
}
