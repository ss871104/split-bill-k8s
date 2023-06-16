package com.menstalk.billqueryservice.services.impl;

import com.menstalk.billqueryservice.dto.BillResponse;
import com.menstalk.billqueryservice.handler.CustomException;
import com.menstalk.billqueryservice.mapper.BillConvert;
import com.menstalk.billqueryservice.models.Bill;
import com.menstalk.billqueryservice.repositories.BillRepository;
import com.menstalk.billqueryservice.services.BillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final BillConvert billConvert;

    @Override
    public List<BillResponse> getAllByPartyId(Long partyId, Integer pageNumber) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("createTime").descending());
            Page<Bill> page;
            List<BillResponse> billResponses = new ArrayList<>();

            try {
                page = billRepository.findAllByPartyId(partyId, pageable);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            if (page.getNumberOfElements() == 0) {
                return billResponses;
            }

            return page.getContent().stream()
                    .map(billConvert::billConvertToBillResponse)
                    .collect(Collectors.toList());
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public List<BillResponse> getAllBills(Integer pageNumber) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, 20, Sort.by("billId").descending());
            Page<Bill> page;

            try {
                page = billRepository.findAll(pageable);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            if (page.getNumberOfElements() == 0) {
                throw new CustomException("no data!");
            }

            return page.getContent().stream()
                    .map(billConvert::billConvertToBillResponse)
                    .collect(Collectors.toList());
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }
}
