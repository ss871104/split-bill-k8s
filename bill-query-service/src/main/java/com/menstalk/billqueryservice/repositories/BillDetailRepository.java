package com.menstalk.billqueryservice.repositories;

import com.menstalk.billqueryservice.models.BillDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillDetailRepository extends JpaRepository<BillDetail, Long> {
    void deleteAllByBillId(Long billId);
    List<BillDetail> findByBillId(Long billId);
}
