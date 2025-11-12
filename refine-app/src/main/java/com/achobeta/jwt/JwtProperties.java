package com.achobeta.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * 密钥
     */
    private String secret;

    /**
     * access-token配置
     */
    private Duration accessTokenTtl = Duration.ofHours(2);

    /**
     * refresh-token配置
     */
    private Duration refreshTokenTtl = Duration.ofDays(2);

}