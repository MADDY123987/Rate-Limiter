package com.RedisSpringCloudGatewayLimiter.RateLimiter.Service;


import com.RedisSpringCloudGatewayLimiter.RateLimiter.Config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

@Service
@RequiredArgsConstructor
public class RedisBucketTokenServiceImpl {

    private final JedisPool jedisPool;
    private final RateLimiterProperties rateLimiterProperties;

    private final String TOKENS_PER_PREFIX="rate_limiter:tokens:";
    private static  final String LAST_REFILL_KEY_PREFIX="rate_limiter:last_refiill:";


}
