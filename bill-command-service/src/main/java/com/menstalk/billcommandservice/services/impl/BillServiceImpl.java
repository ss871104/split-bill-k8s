package com.menstalk.billcommandservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.billcommandservice.dto.BalanceUpdateRequest;
import com.menstalk.billcommandservice.dto.BillPlacedRequest;
import com.menstalk.billcommandservice.dto.BillUpdateRequest;
import com.menstalk.billcommandservice.handler.CustomException;
import com.menstalk.billcommandservice.kafka.event.AddBillEvent;
import com.menstalk.billcommandservice.kafka.event.NewBillEvent;
import com.menstalk.billcommandservice.models.Bill;
import com.menstalk.billcommandservice.models.BillDetail;
import com.menstalk.billcommandservice.models.BillDetailType;
import com.menstalk.billcommandservice.proxy.MemberProxy;
import com.menstalk.billcommandservice.repositories.BillDetailRepository;
import com.menstalk.billcommandservice.repositories.BillRepository;
import com.menstalk.billcommandservice.services.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final BillDetailRepository billDetailRepository;
    private final MemberProxy memberProxy;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void addBillTransfer(BillPlacedRequest billPlacedRequest) {
        try {
            Bill bill = new Bill();
            List<BillDetail> billDetailListExpense = new ArrayList<>();
            List<BillDetail> billDetailListIncome = new ArrayList<>();
            List<BillDetail> billDetailListCombine = new ArrayList<>();
            List<BalanceUpdateRequest> balanceUpdateRequestList = new ArrayList<>();
            AddBillEvent addBillEvent = new AddBillEvent();
            NewBillEvent newBillEvent = new NewBillEvent();
            boolean isFeignSuccess;

            bill = Bill.builder()
                    .partyId(billPlacedRequest.getPartyId())
                    .billName(billPlacedRequest.getBillName())
                    .billType(billPlacedRequest.getBillType())
                    .totalAmount(billPlacedRequest.getTotalAmount())
                    .createTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();
            try {
                bill = billRepository.save(bill);
                billRepository.flush();
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }
            Long billId = bill.getBillId();

            billDetailListExpense = billPlacedRequest.getMemberIdMapExpense().entrySet().stream()
                    .map(x -> BillDetail.builder()
                            .billId(billId)
                            .billDetailType(BillDetailType.EXPENSE)
                            .memberId(x.getKey())
                            .amount(x.getValue())
                            .build())
                    .collect(Collectors.toList());
            billDetailListIncome = billPlacedRequest.getMemberIdMapIncome().entrySet().stream()
                    .map(x -> BillDetail.builder()
                            .billId(billId)
                            .billDetailType(BillDetailType.INCOME)
                            .memberId(x.getKey())
                            .amount(x.getValue())
                            .build())
                    .collect(Collectors.toList());

            billDetailListCombine.addAll(billDetailListExpense);
            billDetailListCombine.addAll(billDetailListIncome);
            try {
                billDetailListCombine = billDetailRepository.saveAll(billDetailListCombine);
                billDetailRepository.flush();
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            balanceUpdateRequestList = billDetailListCombine.stream()
                    .map(x -> BalanceUpdateRequest.builder()
                            .memberId(x.getMemberId())
                            .amount(x.getAmount())
                            .billDetailType(x.getBillDetailType())
                            .build())
                    .collect(Collectors.toList());

            try {
                log.info("Retrieved updateBalance feign");
                isFeignSuccess = Boolean.TRUE.equals(memberProxy.updateBalance(balanceUpdateRequestList).getBody());
            } catch (Exception e) {
                log.error("updateBalance feign error, error: {}", e.toString());
                throw new CustomException("FEIGN ERROR");
            }
            if (!isFeignSuccess) {
                log.error("updateBalance feign return false");
                throw new CustomException("FEIGN RETURN FALSE");
            }

            String billJson = objectMapper.writeValueAsString(bill);
            String billDetailJson = objectMapper.writeValueAsString(billDetailListCombine);

            addBillEvent = AddBillEvent.builder()
                    .bill(billJson)
                    .billDetails(billDetailJson)
                    .build();
            newBillEvent.setPartyId(billPlacedRequest.getPartyId());

            String addBillEventString = objectMapper.writeValueAsString(addBillEvent);
            String newBillEventString = objectMapper.writeValueAsString(newBillEvent);

            try {
                log.info("Send addBillTopic");
                kafkaTemplate.send("addBillTopic", addBillEventString);
            } catch (Exception e) {
                log.error("Failed sending addBillTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
            try {
                log.info("Send newBillTopic");
                kafkaTemplate.send("newBillTopic", newBillEventString);
            } catch (Exception e) {
                log.error("Failed sending newBillTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void addBillAA(BillPlacedRequest billPlacedRequest) {
        try {
            Bill bill = new Bill();
            List<BillDetail> billDetailListExpense = new ArrayList<>();
            List<BillDetail> billDetailListIncome = new ArrayList<>();
            List<BillDetail> billDetailListCombine = new ArrayList<>();
            List<BalanceUpdateRequest> balanceUpdateRequestList = new ArrayList<>();
            AddBillEvent addBillEvent = new AddBillEvent();
            NewBillEvent newBillEvent = new NewBillEvent();
            boolean isFeignSuccess;

            bill = Bill.builder()
                    .partyId(billPlacedRequest.getPartyId())
                    .billName(billPlacedRequest.getBillName())
                    .billType(billPlacedRequest.getBillType())
                    .totalAmount(billPlacedRequest.getTotalAmount())
                    .createTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();
            try {
                bill = billRepository.save(bill);
                billRepository.flush();
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            final var billId = bill.getBillId();
            final var totalAmount = bill.getTotalAmount();

            if (totalAmount % billPlacedRequest.getMemberIdMapExpense().size() == 0) {
                billDetailListExpense = billPlacedRequest.getMemberIdMapExpense().entrySet().stream()
                        .map(x -> BillDetail.builder()
                                .billId(billId)
                                .billDetailType(BillDetailType.EXPENSE)
                                .memberId(x.getKey())
                                .amount(totalAmount / billPlacedRequest.getMemberIdMapExpense().size())
                                .build())
                        .collect(Collectors.toList());
            } else {
                int remain = (int) (totalAmount % billPlacedRequest.getMemberIdMapExpense().size());
                List<Long> memberIdList = new ArrayList<>(billPlacedRequest.getMemberIdMapExpense().keySet());
                List<Long> unluckyMembers = new ArrayList<>();

                for (int i = 0; i < remain; i++) {
                    unluckyMembers.add(memberIdList.get((int) (Math.random() * memberIdList.size())));
                    memberIdList.remove(unluckyMembers.get(i));
                }

                List<BillDetail> unluckyList = billPlacedRequest.getMemberIdMapExpense().keySet().stream()
                        .filter(unluckyMembers::contains)
                        .map(x -> BillDetail.builder()
                                .billId(billId).billDetailType(BillDetailType.EXPENSE)
                                .memberId(x)
                                .amount((totalAmount / billPlacedRequest.getMemberIdMapExpense().size()) + 1)
                                .build())
                        .toList();
                List<BillDetail> luckyList = billPlacedRequest.getMemberIdMapExpense().keySet().stream()
                        .filter(x -> !unluckyMembers.contains(x))
                        .map(x -> BillDetail.builder()
                                .billId(billId).billDetailType(BillDetailType.EXPENSE)
                                .memberId(x)
                                .amount((totalAmount / billPlacedRequest.getMemberIdMapExpense().size()))
                                .build())
                        .toList();

                billDetailListExpense.addAll(unluckyList);
                billDetailListExpense.addAll(luckyList);
            }
            billDetailListIncome = billPlacedRequest.getMemberIdMapIncome().entrySet().stream()
                    .map(x -> BillDetail.builder()
                            .billId(billId)
                            .billDetailType(BillDetailType.INCOME)
                            .memberId(x.getKey())
                            .amount(x.getValue())
                            .build())
                    .collect(Collectors.toList());

            billDetailListCombine.addAll(billDetailListExpense);
            billDetailListCombine.addAll(billDetailListIncome);
            try {
                billDetailListCombine = billDetailRepository.saveAll(billDetailListCombine);
                billDetailRepository.flush();
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            balanceUpdateRequestList = billDetailListCombine.stream()
                    .map(x -> BalanceUpdateRequest.builder()
                            .memberId(x.getMemberId())
                            .amount(x.getAmount())
                            .billDetailType(x.getBillDetailType())
                            .build())
                    .collect(Collectors.toList());

            try {
                log.info("Retrieved updateBalance feign");
                isFeignSuccess = Boolean.TRUE.equals(memberProxy.updateBalance(balanceUpdateRequestList).getBody());
            } catch (Exception e) {
                log.error("updateBalance feign error, error: {}", e.toString());
                throw new CustomException("FEIGN ERROR");
            }
            if (!isFeignSuccess) {
                log.error("updateBalance feign return false");
                throw new CustomException("FEIGN RETURN FALSE");
            }

            String billJson = objectMapper.writeValueAsString(bill);
            String billDetailJson = objectMapper.writeValueAsString(billDetailListCombine);

            addBillEvent = AddBillEvent.builder()
                    .bill(billJson)
                    .billDetails(billDetailJson)
                    .build();
            newBillEvent.setPartyId(billPlacedRequest.getPartyId());

            String addBillEventString = objectMapper.writeValueAsString(addBillEvent);
            String newBillEventString = objectMapper.writeValueAsString(newBillEvent);

            try {
                log.info("Send addBillTopic");
                kafkaTemplate.send("addBillTopic", addBillEventString);
            } catch (Exception e) {
                log.error("Failed sending addBillTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
            try {
                log.info("Send newBillTopic");
                kafkaTemplate.send("newBillTopic", newBillEventString);
            } catch (Exception e) {
                log.error("Failed sending newBillTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void addBillGoDutch(BillPlacedRequest billPlacedRequest) {
        try {
            Bill bill = new Bill();
            List<BillDetail> billDetailListExpense = new ArrayList<>();
            List<BillDetail> billDetailListIncome = new ArrayList<>();
            List<BillDetail> billDetailListCombine = new ArrayList<>();
            List<BalanceUpdateRequest> balanceUpdateRequestList = new ArrayList<>();
            AddBillEvent addBillEvent = new AddBillEvent();
            NewBillEvent newBillEvent = new NewBillEvent();
            boolean isFeignSuccess;

            bill = Bill.builder()
                    .partyId(billPlacedRequest.getPartyId())
                    .billName(billPlacedRequest.getBillName())
                    .billType(billPlacedRequest.getBillType())
                    .totalAmount(billPlacedRequest.getTotalAmount())
                    .createTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                    .build();
            try {
                bill = billRepository.save(bill);
                billRepository.flush();
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            final var billId = bill.getBillId();

            billDetailListExpense = billPlacedRequest.getMemberIdMapExpense().entrySet().stream()
                    .map(x -> BillDetail.builder()
                            .billId(billId)
                            .billDetailType(BillDetailType.EXPENSE)
                            .memberId(x.getKey())
                            .amount(x.getValue())
                            .build())
                    .collect(Collectors.toList());
            billDetailListIncome = billPlacedRequest.getMemberIdMapIncome().entrySet().stream()
                    .map(x -> BillDetail.builder()
                            .billId(billId)
                            .billDetailType(BillDetailType.INCOME)
                            .memberId(x.getKey())
                            .amount(x.getValue())
                            .build())
                    .collect(Collectors.toList());

            billDetailListCombine.addAll(billDetailListExpense);
            billDetailListCombine.addAll(billDetailListIncome);
            try {
                billDetailListCombine = billDetailRepository.saveAll(billDetailListCombine);
                billDetailRepository.flush();
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            balanceUpdateRequestList = billDetailListCombine.stream()
                    .map(x -> BalanceUpdateRequest.builder()
                            .memberId(x.getMemberId())
                            .amount(x.getAmount())
                            .billDetailType(x.getBillDetailType())
                            .build())
                    .collect(Collectors.toList());

            try {
                log.info("Retrieved updateBalance feign");
                isFeignSuccess = Boolean.TRUE.equals(memberProxy.updateBalance(balanceUpdateRequestList).getBody());
            } catch (Exception e) {
                log.error("updateBalance feign error, error: {}", e.toString());
                throw new CustomException("FEIGN ERROR");
            }
            if (!isFeignSuccess) {
                log.error("updateBalance feign return false");
                throw new CustomException("FEIGN RETURN FALSE");
            }

            String billJson = objectMapper.writeValueAsString(bill);
            String billDetailJson = objectMapper.writeValueAsString(billDetailListCombine);

            addBillEvent = AddBillEvent.builder()
                    .bill(billJson)
                    .billDetails(billDetailJson)
                    .build();
            newBillEvent.setPartyId(billPlacedRequest.getPartyId());

            String addBillEventString = objectMapper.writeValueAsString(addBillEvent);
            String newBillEventString = objectMapper.writeValueAsString(newBillEvent);

            try {
                log.info("Send addBillTopic");
                kafkaTemplate.send("addBillTopic", addBillEventString);
            } catch (Exception e) {
                log.error("Failed sending addBillTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
            try {
                log.info("Send newBillTopic");
                kafkaTemplate.send("newBillTopic", newBillEventString);
            } catch (Exception e) {
                log.error("Failed sending newBillTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void updateBillTransfer(BillUpdateRequest billUpdateRequest) {
        try {
            Bill bill = new Bill();
            BillPlacedRequest billPlacedRequest = new BillPlacedRequest();

            try {
                bill = billRepository.findById(billUpdateRequest.getBillId()).orElseThrow(() -> new CustomException("bill not found"));
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            billPlacedRequest = BillPlacedRequest.builder()
                    .partyId(bill.getPartyId())
                    .billName(bill.getBillName())
                    .billType(billUpdateRequest.getBillType())
                    .totalAmount(billUpdateRequest.getTotalAmount())
                    .memberIdMapExpense(billUpdateRequest.getMemberIdMapExpense())
                    .memberIdMapIncome(billUpdateRequest.getMemberIdMapIncome())
                    .build();

            removeBill(billUpdateRequest.getBillId());
            addBillTransfer(billPlacedRequest);

        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void updateBillAA(BillUpdateRequest billUpdateRequest) {
        try {
            Bill bill = new Bill();
            BillPlacedRequest billPlacedRequest = new BillPlacedRequest();

            try {
                bill = billRepository.findById(billUpdateRequest.getBillId()).orElseThrow(() -> new CustomException("bill not found"));
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            billPlacedRequest = BillPlacedRequest.builder()
                    .partyId(bill.getPartyId())
                    .billName(bill.getBillName())
                    .billType(billUpdateRequest.getBillType())
                    .totalAmount(billUpdateRequest.getTotalAmount())
                    .memberIdMapExpense(billUpdateRequest.getMemberIdMapExpense())
                    .memberIdMapIncome(billUpdateRequest.getMemberIdMapIncome())
                    .build();

            removeBill(billUpdateRequest.getBillId());
            addBillAA(billPlacedRequest);

        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void updateBillGoDutch(BillUpdateRequest billUpdateRequest) {
        try {
            Bill bill = new Bill();
            BillPlacedRequest billPlacedRequest = new BillPlacedRequest();

            try {
                bill = billRepository.findById(billUpdateRequest.getBillId()).orElseThrow(() -> new CustomException("bill not found"));
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            billPlacedRequest = BillPlacedRequest.builder()
                    .partyId(bill.getPartyId())
                    .billName(bill.getBillName())
                    .billType(billUpdateRequest.getBillType())
                    .totalAmount(billUpdateRequest.getTotalAmount())
                    .memberIdMapExpense(billUpdateRequest.getMemberIdMapExpense())
                    .memberIdMapIncome(billUpdateRequest.getMemberIdMapIncome())
                    .build();

            removeBill(billUpdateRequest.getBillId());
            addBillGoDutch(billPlacedRequest);

        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }

    @Override
    public void removeBill(Long billId) {
        try {
            List<BillDetail> billDetailList = new ArrayList<>();
            List<BalanceUpdateRequest> balanceUpdateRequests = new ArrayList<>();
            boolean isFeignSuccess;

            try {
                billDetailList = billDetailRepository.findByBillId(billId);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            try {
                billDetailRepository.deleteAllByBillId(billId);
                billRepository.deleteById(billId);
            } catch (Exception e) {
                throw new CustomException("DB ERROR");
            }

            billDetailList.forEach(x -> {
                if (x.getBillDetailType() == BillDetailType.EXPENSE) {
                    x.setBillDetailType(BillDetailType.INCOME);
                } else {
                    x.setBillDetailType(BillDetailType.EXPENSE);
                }
                balanceUpdateRequests.add(BalanceUpdateRequest.builder()
                        .memberId(x.getMemberId())
                        .amount(x.getAmount())
                        .billDetailType(x.getBillDetailType())
                        .build());
            });

            try {
                log.info("Retrieved updateBalance feign");
                isFeignSuccess = Boolean.TRUE.equals(memberProxy.updateBalance(balanceUpdateRequests).getBody());
            } catch (Exception e) {
                log.error("updateBalance feign error, error: {}", e.toString());
                throw new CustomException("FEIGN ERROR");
            }
            if (!isFeignSuccess) {
                log.error("updateBalance feign return false");
                throw new CustomException("FEIGN RETURN FALSE");
            }

            try {
                log.info("Send removeBillTopic");
                kafkaTemplate.send("removeBillTopic", billId);
            } catch (Exception e) {
                log.error("Failed sending removeBillTopic, error: {}", e.toString());
                throw new CustomException("KAFKA ERROR");
            }
        } catch (CustomException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.toString());
        }
    }
}
