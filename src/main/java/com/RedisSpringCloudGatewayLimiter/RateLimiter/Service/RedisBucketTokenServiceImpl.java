package com.RedisSpringCloudGatewayLimiter.RateLimiter.Service;


import com.RedisSpringCloudGatewayLimiter.RateLimiter.Config.RateLimiterProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@RequiredArgsConstructor
public class RedisBucketTokenServiceImpl {

    private final JedisPool jedisPool;
    private final RateLimiterProperties rateLimiterProperties;

    private final String TOKENS_PER_PREFIX="rate_limiter:tokens:";
    private static  final String LAST_REFILL_KEY_PREFIX="rate_limiter:last_refiill:";

    public Boolean isAllowed(String clientId) //decides Allow or Reject
    {
        String tokenKey=TOKENS_PER_PREFIX+clientId;
        try(Jedis jedis=jedisPool.getResource())
        {
            refillToken(clientId,jedis);
            String tokenStr=jedis.get(tokenKey);

            long currentTokens=tokenStr!=null
                    ?Long.parseLong(tokenStr)
                    : rateLimiterProperties.getCapacity();


            if(currentTokens<=0)
            {
                return false;
            }
            long remaning=jedis.decr(tokenKey);
            return remaning>=0;
        }
    }
    public long getCapacity(String clientId)
    {
        return rateLimiterProperties.getCapacity();
    }
    public long getAvailable(String clientId)
    {
        String tokenkey=TOKENS_PER_PREFIX+clientId;

        try(Jedis jedis=jedisPool.getResource())
        {
            refillToken(clientId,jedis);
            String tokenStr=jedis.get(tokenkey);
            return tokenStr!=null
                    ?Long.parseLong(tokenStr)
                    : rateLimiterProperties.getCapacity();
        }
    }
    private void refillToken(String clientId,Jedis jedis)
    {
        String tokenKey=TOKENS_PER_PREFIX+clientId;
        String lastRefillKey=LAST_REFILL_KEY_PREFIX+clientId;

        long now=System.currentTimeMillis();

        String lastRefillStr=jedis.get(lastRefillKey);

        if(lastRefillStr==null)
        {
            jedis.set(tokenKey,String.valueOf(rateLimiterProperties.getCapacity()));
            jedis.set(lastRefillKey,String.valueOf(now));
            return;
        }
        long lastRefillTime=Long.parseLong(lastRefillStr);
        long elapsedTime=now-lastRefillTime;

        if(elapsedTime<=0)
        {
            return;
        }
        long tokensToadd=(elapsedTime* rateLimiterProperties.getRefillRate())/1000;

        if(tokensToadd<=0)
        {
            return;
        }
        String tokenStr=jedis.get(tokenKey);
        long currentTokens=tokenStr!=null
                ?Long.parseLong(tokenStr)
                :rateLimiterProperties.getCapacity();

        long newTokens=Math.min(
                rateLimiterProperties.getCapacity(),
                currentTokens+tokensToadd
        );

        jedis.set(tokenKey,String.valueOf(newTokens));
        jedis.set(lastRefillKey,String.valueOf(now));

    }
}
