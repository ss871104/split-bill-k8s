package com.menstalk.billqueryservice.services.impl;

import com.menstalk.billqueryservice.dto.BillDetailResponse;
import com.menstalk.billqueryservice.handler.CustomException;
import com.menstalk.billqueryservice.mapper.BillConvert;
import com.menstalk.billqueryservice.models.BillDetail;
import com.menstalk.billqueryservice.repositories.BillDetailRepository;
import com.menstalk.billqueryservice.services.BillDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BillDetailServiceImpl implements BillDetailService {
    private final BillDetailRepository billDetailRepository;
    private final BillConvert billConvert;

    @Override
    public List<BillDetailResponse> getAllByBillId(Long billId) {
        try {
            List<BillDetail> billDetails = new ArrayList<>();
            List<BillDetailResponse> billDetailResponses = new ArrayList<>();

            try {
                billDetails = billDetailRepository.findByBillId(billId);

                billDetailResponses = billDetails.stream()
                        .map(billConvert::billDetailConvertToBillDetailResponse)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            return billDetailResponses;
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

}
