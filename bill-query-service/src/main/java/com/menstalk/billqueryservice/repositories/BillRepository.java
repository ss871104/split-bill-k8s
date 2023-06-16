package com.menstalk.billqueryservice.repositories;

import com.menstalk.billqueryservice.models.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Page<Bill> findAllByPartyId(Long partyId, Pageable pageable);
}
