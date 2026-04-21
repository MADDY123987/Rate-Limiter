package com.RedisSpringCloudGatewayLimiter.RateLimiter.Config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {
    private long capacity= 10;
    private long refillRate= 5;
    private String apiServerUrl="https://localhost:8080";
    private int timeout=5000;

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(long refillRate) {
        this.refillRate = refillRate;
    }

    public String getApiServerUrl() {
        return apiServerUrl;
    }

    public void setApiServerUrl(String apiServerUrl) {
        this.apiServerUrl = apiServerUrl;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
