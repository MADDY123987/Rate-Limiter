package com.RedisSpringCloudGatewayLimiter.RateLimiter.Config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {
    private long capacity= 10;
    private long refillRate= 5;
    private String apiServerUrl="https://localhost:8080";
    private int timeout=5000;
}
