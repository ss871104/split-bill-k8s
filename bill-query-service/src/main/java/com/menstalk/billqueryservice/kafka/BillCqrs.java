package com.menstalk.billqueryservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.billqueryservice.kafka.event.AddBillEvent;
import com.menstalk.billqueryservice.models.Bill;
import com.menstalk.billqueryservice.models.BillDetail;
import com.menstalk.billqueryservice.repositories.BillDetailRepository;
import com.menstalk.billqueryservice.repositories.BillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillCqrs {
    private final BillRepository billRepository;
    private final BillDetailRepository billDetailRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "addBillTopic")
    public void addBillEvent(String addBillEventString) throws JsonProcessingException {
        AddBillEvent addBillEvent = objectMapper.readValue(addBillEventString, AddBillEvent.class);

        Bill bill = objectMapper.readValue(addBillEvent.getBill(), Bill.class);
        List<BillDetail> billDetails = objectMapper.readValue(addBillEvent.getBillDetails(), new TypeReference<List<BillDetail>>(){});
        log.info("Received bill cqrs from addBillEvent, billId: {}, partyId: {}!", bill.getBillId(), bill.getPartyId());

        try {
            billRepository.save(bill);
            billDetailRepository.saveAll(billDetails);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Bill cqrs from addBillEvent failed, billId: {}, partyId: {}, error: {}", bill.getBillId(), bill.getPartyId(), e.toString());
        }

    }

    @KafkaListener(topics = "removeBillTopic")
    public void removeBillEvent(Long billId) {
        log.info("Received bill event from bill-command-service, billId: {}!", billId);
        try {
            billDetailRepository.deleteAllByBillId(billId);
            billRepository.deleteById(billId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Received bill event from bill-command-service, billId: {} removal failed, error: {}", billId, e.toString());
        }

    }

}
