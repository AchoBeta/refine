package com.achobeta.config;

import com.achobeta.intercepter.LogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author BanTanger 半糖
 * @date 2024/11/4
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.config.cross-origin}")
    private String crossOrigin;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 所有的请求都加上 Log 拦截器
        registry.addInterceptor(new LogInterceptor());
    }


    /**
     * 全局跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 允许访问的地址/域名, 配置文件里的app.config.allow-origin
                .allowedOriginPatterns(crossOrigin)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Bean(name = "mvcAsyncTaskExecutor")
    public AsyncTaskExecutor mvcAsyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cpuCore = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cpuCore + 1); // 框架层面线程数精简
        executor.setMaxPoolSize(2 * cpuCore + 1);
        executor.setQueueCapacity(50); // 避免请求堆积
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("mvc-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcAsyncTaskExecutor());
        configurer.setDefaultTimeout(60 * 1000L); // 异步请求超时（适配 AI 长流式响应）
    }

}
