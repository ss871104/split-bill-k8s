package com.menstalk.batchservice.proxy;

import com.menstalk.batchservice.dto.MemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

//@FeignClient(name = "master-query-service", url = "http://localhost:8200")
@FeignClient(name = "master-query-service", url = "http://master-query-service.menstalk.svc.cluster.local:8200")
public interface MasterQueryProxy {

    @GetMapping("/api/member/partyId/{partyId}")
    ResponseEntity<List<MemberResponse>> getMembersByPartyId(@PathVariable("partyId") Long partyId);

    @GetMapping("/api/member/owedWeekly")
    ResponseEntity<List<MemberResponse>> getMembersForOwedWeeklyNotification();
}
