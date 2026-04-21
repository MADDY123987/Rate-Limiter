package com.RedisSpringCloudGatewayLimiter.RateLimiter.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.RedisSpringCloudGatewayLimiter.RateLimiter.Service.RateLimiterService;
import java.util.Map;

@RestController
public class StatusController {

    private final RateLimiterService rateLimiterService;

    public StatusController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    // ✅ Health endpoint
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Rate Limiter Gateway"
        )));
    }

    // ✅ Rate limit status endpoint
    @GetMapping("/rate-limit/status")
    public Mono<ResponseEntity<Map<String, Object>>> getRateLimitingStatus(ServerWebExchange exchange) {

        String clientId = getClientId(exchange);

        return Mono.fromSupplier(() -> ResponseEntity.ok(Map.of(
                "clientId", clientId,
                "capacity", rateLimiterService.getCapacity(clientId),
                "availableTokens", rateLimiterService.getAvailableTokens(clientId)
        )));
    }
    @GetMapping("/test")
    public Mono<ResponseEntity<String>> test(ServerWebExchange exchange) {

        String clientId = getClientId(exchange);

        return Mono.fromSupplier(() -> {
            boolean allowed = rateLimiterService.isAllowed(clientId);

            if (!allowed) {
                return ResponseEntity.status(429).body("Too many requests");
            }

            return ResponseEntity.ok("Request allowed");
        });
    }

    // ✅ Extract client IP
    private String getClientId(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        var remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }
}