package com.RedisSpringCloudGatewayLimiter.RateLimiter.filter;

import com.RedisSpringCloudGatewayLimiter.RateLimiter.Service.RateLimiterService;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

@Component
public class TokenBucketRateLimiterFilter
        extends AbstractGatewayFilterFactory<TokenBucketRateLimiterFilter.Config> {

    private final RateLimiterService rateLimiterService;

    public TokenBucketRateLimiterFilter(RateLimiterService rateLimiterService) {
        super(Config.class); // ✅ IMPORTANT FIX
        this.rateLimiterService = rateLimiterService;
    }

    // ✅ FIX: proper Config class (no static method here)
    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            String clientId = getClientId(request);

            // ❌ BLOCK request
            if (!rateLimiterService.isAllowed(clientId)) {

                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

                addRateLimitHeaders(response, clientId);

                String errorBody = String.format(
                        "{\"error\":\"Rate limit exceeded\",\"clientId\":\"%s\"}",
                        clientId
                );

                return response.writeWith(
                        Mono.just(response.bufferFactory()
                                .wrap(errorBody.getBytes(StandardCharsets.UTF_8)))
                );
            }

            // ✅ ALLOW request
            return chain.filter(exchange).then(
                    Mono.fromRunnable(() -> {
                        addRateLimitHeaders(response, clientId);
                    })
            );
        };
    }

    private void addRateLimitHeaders(ServerHttpResponse response, String clientId) {

        response.getHeaders().add("X-RateLimit-Limit",
                String.valueOf(rateLimiterService.getCapacity(clientId)));

        response.getHeaders().add("X-RateLimit-Remaining",
                String.valueOf(rateLimiterService.getAvailableTokens(clientId)));
    }

    private String getClientId(ServerHttpRequest request) {

        var remoteAddress = request.getRemoteAddress();

        if (remoteAddress != null && remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }
}