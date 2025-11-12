package com.achobeta.config;

import com.achobeta.intercepter.LogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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

}
