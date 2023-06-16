package com.menstalk.apigateway.jwt;

import com.menstalk.apigateway.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthorizationClient {

    public Mono<AuthResponse> findByUsername(String username, ServerWebExchange exchange) {
        log.info("Retrieve authentication feign");
        WebClient webClient = WebClient.create();
        String url = "http://" + exchange.getRequest().getURI().getHost() + "/auth-service/api/auth/authentication";

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(username))
                .retrieve()
                .bodyToMono(AuthResponse.class);
    }

    public Mono<Boolean> ifTokenInBlackList(String token, ServerWebExchange exchange) {
        log.info("Retrieve checkBlackList feign");
        WebClient webClient = WebClient.create();
        String url = "http://" + exchange.getRequest().getURI().getHost() + "/auth-service/api/auth/checkBlackList";

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(token))
                .retrieve()
                .bodyToMono(Boolean.class);
    }

}
