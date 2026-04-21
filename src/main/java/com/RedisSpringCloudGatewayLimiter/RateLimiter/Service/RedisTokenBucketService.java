package com.RedisSpringCloudGatewayLimiter.RateLimiter.Service;


import com.RedisSpringCloudGatewayLimiter.RateLimiter.Config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisTokenBucketService {

    private final JedisPool jedisPool;
    private final RateLimiterProperties rateLimiterProperties;

    private final String TOKENS_PER_PREFIX = "rate_limiter:tokens:";
    private static final String LAST_REFILL_KEY_PREFIX = "rate_limiter:last_refill:";

    public RedisTokenBucketService(JedisPool jedisPool, RateLimiterProperties rateLimiterProperties) {
        this.jedisPool = jedisPool;
        this.rateLimiterProperties = rateLimiterProperties;
    }

    public boolean isAllowed(String clientId) {
        String tokenKey = TOKENS_PER_PREFIX + clientId;

        try (Jedis jedis = jedisPool.getResource()) {
            refillToken(clientId, jedis);

            String tokenStr = jedis.get(tokenKey);

            if (tokenStr == null) {
                long capacity = rateLimiterProperties.getCapacity();
                jedis.set(tokenKey, String.valueOf(capacity));
                tokenStr = String.valueOf(capacity);
            }

            long currentTokens = Long.parseLong(tokenStr);

            if (currentTokens <= 0) {
                return false;
            }

            long remaining = jedis.decr(tokenKey);
            return remaining >= 0;
        }
    }

    public long getCapacity(String clientId) {
        return rateLimiterProperties.getCapacity();
    }
    public long getAvailableTokens(String clientId) {
        String tokenKey = TOKENS_PER_PREFIX + clientId;

        try (Jedis jedis = jedisPool.getResource()) {
            refillToken(clientId, jedis);

            String tokenStr = jedis.get(tokenKey);
            return tokenStr != null
                    ? Long.parseLong(tokenStr)
                    : rateLimiterProperties.getCapacity();
        }
    }

    private void refillToken(String clientId, Jedis jedis) {
        String tokenKey = TOKENS_PER_PREFIX + clientId;
        String lastRefillKey = LAST_REFILL_KEY_PREFIX + clientId;

        long now = System.currentTimeMillis();
        String lastRefillStr = jedis.get(lastRefillKey);

        if (lastRefillStr == null) {
            jedis.set(tokenKey, String.valueOf(rateLimiterProperties.getCapacity()));
            jedis.set(lastRefillKey, String.valueOf(now));
            return;
        }

        long lastRefillTime = Long.parseLong(lastRefillStr);
        long elapsedTime = now - lastRefillTime;

        if (elapsedTime <= 0) return;

        long tokensToAdd = (elapsedTime * rateLimiterProperties.getRefillRate()) / 1000;

        if (tokensToAdd <= 0) return;

        String tokenStr = jedis.get(tokenKey);
        long currentTokens = tokenStr != null
                ? Long.parseLong(tokenStr)
                : rateLimiterProperties.getCapacity();

        long newTokens = Math.min(
                rateLimiterProperties.getCapacity(),
                currentTokens + tokensToAdd
        );

        jedis.set(tokenKey, String.valueOf(newTokens));
        jedis.set(lastRefillKey, String.valueOf(now));
    }
}