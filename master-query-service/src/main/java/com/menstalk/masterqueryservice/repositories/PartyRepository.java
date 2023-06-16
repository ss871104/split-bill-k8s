package com.menstalk.masterqueryservice.repositories;

import com.menstalk.masterqueryservice.models.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
}
