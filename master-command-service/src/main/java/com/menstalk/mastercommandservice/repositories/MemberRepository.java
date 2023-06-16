package com.menstalk.mastercommandservice.repositories;

import com.menstalk.mastercommandservice.models.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT m FROM Member m WHERE m.partyId = :partyId AND m.memberStatus = 1")
    List<Member> findActiveMembersByPartyId(@Param("partyId") Long partyId);

    @Query("SELECT m FROM Member m WHERE m.partyId = :partyId AND m.userId = :userId AND m.memberStatus = 1")
    Member findActiveMemberByPartyIdAndUserId(@Param("partyId") Long partyId, @Param("userId") Long userId);

    @Query("SELECT m FROM Member m WHERE m.partyId = :partyId AND m.userId = :userId")
    Member findMemberByPartyIdAndUserId(@Param("partyId") Long partyId, @Param("userId") Long userId);
}
