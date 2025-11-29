package com.achobeta.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * AI 专属线程池配置（用于 AI 流式调用、API 请求等业务逻辑）
 * 核心优化：配置容错、参数合理性校验、资源占用控制、生产环境监控
 */
@Configuration
@Slf4j
public class AiThreadPoolConfig {

    /**
     * AI 最大并发数（从配置文件读取，默认值 10，避免配置缺失导致启动失败）
     * 用途：限制 AI 调用的最大并发量，避免触发第三方 API（如 DashScope）限流
     */
    @Value("${ai.dashscope.max-concurrency:10}")
    private int aiMaxConcurrency;

    /**
     * AI 专属线程池（业务层面：执行 AI 流式调用、模型推理等耗时操作）
     */
    @Bean(name = "aiExclusiveThreadPool") // 明确 bean 名称，避免注入冲突
    public ThreadPoolTaskExecutor aiExclusiveThreadPool() {
        // 1. 基础参数计算（CPU 核心数）
        int cpuCore = Runtime.getRuntime().availableProcessors();
        log.info("系统 CPU 核心数：{}，配置的 AI 最大并发数：{}", cpuCore, aiMaxConcurrency);

        // 2. 配置容错校验（避免无效配置）
        if (aiMaxConcurrency < 1) {
            log.warn("AI 最大并发数配置无效（{}），使用默认值 10", aiMaxConcurrency);
            aiMaxConcurrency = 10;
        }

        // 3. 线程池参数优化（核心逻辑）
        // 核心线程数：取「CPU核心数*2 +1」和「AI最大并发数的1/2」的较小值（避免空闲线程浪费）
        int corePoolSize = Math.min(cpuCore * 2 + 1, aiMaxConcurrency / 2);
        // 最大线程数：不超过配置的 AI 最大并发数（避免超预期并发导致限流/资源耗尽）
        int maxPoolSize = Math.min(cpuCore * 4 + 1, aiMaxConcurrency);
        // 队列容量：核心线程数*3（上限 50），平衡缓冲和内存占用（避免队列过大导致任务堆积）
        int queueCapacity = Math.min(corePoolSize * 3, 50);

        // 4. 初始化线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(120); // 空闲线程存活时间（120秒，适配 AI 长耗时调用）
        executor.setThreadNamePrefix("ai-exclusive-thread-"); // 线程名前缀（日志排查）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 拒绝策略

        // 5. 线程池销毁时的优雅关闭（等待任务执行完成，避免任务丢失）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30); // 等待 30 秒后强制关闭（适配 AI 长耗时任务）
        executor.setAwaitTerminationMillis(30 * 1000L); // 兼容低版本 Spring（可选）

        // 6. 初始化并打印配置（生产环境监控关键）
        executor.initialize();
        log.info("AI 专属线程池初始化完成：核心线程数={}，最大线程数={}，队列容量={}，拒绝策略={}",
                corePoolSize, maxPoolSize, queueCapacity, executor.getThreadPoolExecutor().getRejectedExecutionHandler().getClass().getSimpleName());

        return executor;
    }
}