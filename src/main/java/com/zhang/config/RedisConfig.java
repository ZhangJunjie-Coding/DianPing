package com.zhang.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Bean
    public RedissonClient redissonClient() {
        // 配置类
        Config config = new Config();
        // 添加redis地址，这里添加了单点的地址，也可以使用  config.useClusterServers()

        config.useSingleServer().setAddress("redis://10.245.247.185:6379");
        return Redisson.create(config);
    }
    @Bean
    public RedissonClient redissonClient2() {
        // 配置类
        Config config = new Config();
        // 添加redis地址，这里添加了单点的地址，也可以使用  config.useClusterServers()

        config.useSingleServer().setAddress("redis://10.245.247.185:6380");
        return Redisson.create(config);
    }
    @Bean
    public RedissonClient redissonClient3() {
        // 配置类
        Config config = new Config();
        // 添加redis地址，这里添加了单点的地址，也可以使用  config.useClusterServers()

        config.useSingleServer().setAddress("redis://10.245.247.185:6381");
        return Redisson.create(config);
    }
}
