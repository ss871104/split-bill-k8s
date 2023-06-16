package com.menstalk.billcommandservice.proxy;

import com.menstalk.billcommandservice.dto.BalanceUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@FeignClient(name = "master-command-service", url = "http://localhost:8100")
@FeignClient(name = "master-command-service", url = "http://master-command-service.menstalk.svc.cluster.local:8100")
public interface MemberProxy {

    @PutMapping("/api/member/updateBalance")
    ResponseEntity<Boolean> updateBalance(@RequestBody List<BalanceUpdateRequest> billRequests);
}
