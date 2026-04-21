package com.RedisSpringCloudGatewayLimiter.RateLimiter.Config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfig {
    private String host="localhost";
    private int port=6379;
    private int timeout=2000;

    @Bean(name = "jedisPool")
    public JedisPool getJedisPool()
    {
        JedisPoolConfig poolConfig=new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        return new JedisPool(poolConfig,host,port,timeout);
    }
}
