package com.menstalk.mastercommandservice.repositories;

import com.menstalk.mastercommandservice.models.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
}
