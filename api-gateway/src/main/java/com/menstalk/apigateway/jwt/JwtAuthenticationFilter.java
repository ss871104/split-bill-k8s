package com.menstalk.apigateway.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter , Ordered {
    private final JwtUtil jwtUtil;
    private final AuthorizationClient authorizationClient;
    private final JwtExceptionHandler jwtExceptionHandler;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        final List<String> apiEndpoints = List.of("/api/auth", "/swagger-ui", "/v2/api-docs", "/swagger-resources", "/webjars/**");

        Predicate<ServerHttpRequest> isApiSecured = r -> apiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        if (isApiSecured.test(request)) {
            if (!request.getHeaders().containsKey("Authorization")) {

                return jwtExceptionHandler.tokenException(exchange, HttpStatus.UNAUTHORIZED, "No token found");
            }

            final String token = request.getHeaders().getOrEmpty("Authorization").get(0);

            if (!token.startsWith("Bearer ")) {

                return jwtExceptionHandler.tokenException(exchange, HttpStatus.UNAUTHORIZED, "Invalid token");
            }

            final String jwt = token.substring(7);

            this.authorizationClient.ifTokenInBlackList(jwt, exchange)
                    .flatMap(ifTokenInBlackList -> {
                        if (ifTokenInBlackList) {
                            return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Jwt Token blackList");
                        }
                        return Mono.empty();
                    })
                    .subscribe();

            String username;
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Expired JWT token");
            } catch (MalformedJwtException e) {
                return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Invalid JWT token");
            } catch (SignatureException e) {
                return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Invalid JWT signature");
            }

            return this.authorizationClient.findByUsername(username, exchange)
                    .flatMap(userDetails -> {
                        if (username != null && jwtUtil.isTokenValid(jwt, userDetails)) {
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header("Authenticated-User", username)
                                    .build();
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        } else {
                            return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Invalid JWT token");
                        }
                    })
                    .onErrorResume(e -> {
                        if (e instanceof SignatureException) {
                            return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Invalid JWT signature");
                        } else if (e instanceof MalformedJwtException) {
                            return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Invalid JWT token");
                        } else if (e instanceof ExpiredJwtException) {
                            return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Expired JWT token");
                        } else if (e instanceof UnsupportedJwtException) {
                            return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "Unsupported JWT token");
                        } else if (e instanceof IllegalArgumentException) {
                            return jwtExceptionHandler.tokenException(exchange, HttpStatus.BAD_REQUEST, "JWT claims string is empty");
                        } else {
                            // Handle other exceptions
                            return Mono.error(e);
                        }
                    });
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
