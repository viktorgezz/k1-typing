package ru.viktorgezz.avatarmodule.config;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class CommonConfig {

    @Bean
    public ProxyManager<String> proxyManager(
            RedissonClient redissonClient
    ) {
        Redisson redisson = (Redisson) redissonClient;
        CommandAsyncExecutor commandExecutor = redisson.getCommandExecutor();
        return RedissonBasedProxyManager.builderFor(commandExecutor).build();
    }

    @Bean(name = "imageBucketConfig")
    public BucketConfiguration bucketConfigurationForGenerateImage() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                        .capacity(1)
                        .refillIntervally(1, Duration.ofMinutes(1)))
                .build();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    @Bean
    public TaskExecutor taskExecutorAsync() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(7);
        return executor;
    }
}
