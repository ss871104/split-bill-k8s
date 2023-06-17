package com.menstalk.masterqueryservice.repositories;

import com.menstalk.masterqueryservice.models.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m WHERE  m.userId = :userId AND m.memberStatus = 1")
    List<Member> findActiveMemberByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Member m WHERE  m.partyId = :partyId")
    List<Member> findMemberByPartyId(@Param("partyId") Long partyId);

    @Query("SELECT m FROM Member m WHERE m.balance < 0 AND m.memberStatus = 1")
    List<Member> findActiveMemberWithNegativeBalance();
}
