package com.newpick4u.comment.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

  private static final String REDISSON_HOST_PREFIX = "redis://";
  @Value("${spring.data.redis.host}")
  private String redisHost;
  @Value("${spring.data.redis.port}")
  private int redisPort;
  @Value("${spring.data.redis.password}")
  private String password;

  @Bean
  public RedissonClient redissonClient() {
    RedissonClient redisson = null;
    Config config = new Config();
    config.useSingleServer().setAddress(REDISSON_HOST_PREFIX + redisHost + ":" + redisPort)
        .setPassword(password);

    redisson = Redisson.create(config);
    return redisson;
  }
}
