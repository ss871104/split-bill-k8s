package com.menstalk.billcommandservice.repositories;

import com.menstalk.billcommandservice.models.BillDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDetailRepository extends JpaRepository<BillDetail, Long> {
    void deleteAllByBillId(Long billId);
    List<BillDetail> findByBillId(Long billId);
}
