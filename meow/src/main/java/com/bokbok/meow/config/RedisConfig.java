package com.bokbok.meow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        URI uri = URI.create(redisUrl);

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration();
        config.setHostName(uri.getHost());
        config.setPort(uri.getPort());

        // Extract password from URL
        if (uri.getUserInfo() != null) {
            String[] userInfo = uri.getUserInfo().split(":");
            if (userInfo.length > 1) {
                config.setPassword(userInfo[1]);
            }
        }

        LettuceClientConfiguration.LettuceClientConfigurationBuilder
                builder = LettuceClientConfiguration.builder();

        // Enable SSL for Upstash (rediss://)
        if (redisUrl.startsWith("rediss://")) {
            builder.useSsl().disablePeerVerification();
        }

        return new LettuceConnectionFactory(config, builder.build());
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}