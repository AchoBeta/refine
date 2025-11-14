package com.achobeta.jwt;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.achobeta.domain.user.service.Jwt;
import com.achobeta.infrastructure.redis.IRedisService;
import com.achobeta.types.exception.UnauthorizedException;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.achobeta.types.common.Constants.USER_REFRESH_TOKEN_KEY;

@Component
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class JwtTool implements Jwt {

    @Resource
    private IRedisService redis;

    private final JwtProperties jwtProperties;

    private final JWTSigner jwtSigner;


    // HS256 对称加密，字符串密钥创建签名器
    public JwtTool(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.jwtSigner = JWTSignerUtil.hs256(jwtProperties.getSecret().getBytes());
    }

    /**
     * 生成access-token（短期访问令牌）
     * 携带用户ID和权限相关信息
     */
    public String createAccessToken(String userId, Map<String, Object> extraClaims) {
        // 基础载荷,用户Id
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "access"); // 标记Token类型
        // 添加额外载荷（如角色、权限）
        if (extraClaims != null) {
            claims.putAll(extraClaims);
        }
        // 生成Token
        return createToken(claims, jwtProperties.getAccessTokenTtl());
    }

    /**
     * 生成refresh-token（长期刷新令牌）
     * 仅携带用户ID和唯一标识（用于注销）
     */
    public String createRefreshToken(String userId) {
        String jti = UUID.randomUUID().toString(); // 唯一标识，用于注销
        // 载荷：用户ID + 唯一标识 + 类型
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("jti", jti);
        claims.put("type", "refresh"); // 标记Token类型
        // 生成Token
        String refreshToken = createToken(claims, jwtProperties.getRefreshTokenTtl());
        redis.setValue(
                USER_REFRESH_TOKEN_KEY + jti,
                userId,
                jwtProperties.getRefreshTokenTtl().toMillis()
        );
        return refreshToken;
    }

    /**
     * 通用Token生成方法
     */
    private String createToken(Map<String, Object> claims, Duration ttl) {
        claims.put("iat", new Date()); // 添加签发时间
        return JWT.create()
                .addPayloads(claims) // 设置载荷
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis())) // 过期时间
                .setSigner(jwtSigner) // 签名器
                .sign(); // 生成签名
    }

    /**
     * 解析access-token并验证
     *
     * @return 解析后的用户ID
     */
    public String parseAccessToken(String token) {
        TokenPayload payload = parseToken(token, "access");
        return payload.getUserId();
    }

    /**
     * 解析refresh-token并验证（包含Redis状态校验）
     *
     * @return 解析后的用户ID
     */
    public String parseRefreshToken(String token) {
        TokenPayload payload = parseToken(token, "refresh");
        // 校验Redis中是否存在该refresh-token（防止已注销）
        String jti = payload.getJti();
        String redisKey = USER_REFRESH_TOKEN_KEY + jti;
        String userIdStr = redis.getValue(redisKey);
        if (StrUtil.isBlank(userIdStr) || !userIdStr.equals(payload.getUserId())) {
            throw new UnauthorizedException("refresh-token已失效，请重新登录");
        }
        return payload.getUserId();
    }

    /**
     * 通用Token解析方法（校验签名、过期时间、类型）
     */
    private TokenPayload parseToken(String token, String expectedType) {
        // 1. 校验非空
        if (StrUtil.isBlank(token)) {
            throw new UnauthorizedException("未登录：token为空");
        }

        JWT jwt;
        try {
            // 2. 解析Token
            jwt = JWT.of(token).setSigner(jwtSigner);
            // 3. 校验签名和过期时间（Hutool自动处理）
            JWTValidator.of(jwt).validateAlgorithm().validateDate();
            log.debug("Token有效：{}", jwt);
        } catch (ValidateException e) {
            // 细分异常：过期/签名无效
            if (e.getMessage().contains("expired")) {
                throw new UnauthorizedException(expectedType + "-token已过期", e);
            } else {
                throw new UnauthorizedException(expectedType + "-token签名无效", e);
            }
        } catch (Exception e) {
            throw new UnauthorizedException("无效的" + expectedType + "-token：格式错误", e);
        }

        // 4. 校验Token类型
        String actualType = Convert.toStr(jwt.getPayload("type"));
        if (!StrUtil.equals(actualType, expectedType)) {
            throw new UnauthorizedException("无效的" + expectedType + "-token：类型错误");
        }

        // 5. 提取用户ID
        String userId = Convert.toStr(jwt.getPayload("userId"));
        if (userId == null) {
            throw new UnauthorizedException("无效的" + expectedType + "-token：缺失用户ID");
        }

        // 6. 如果是refresh-token，提取jti
        String jti = Convert.toStr(jwt.getPayload("jti"));
        if (StrUtil.equals(expectedType, "refresh") && StrUtil.isBlank(jti)) {
            throw new UnauthorizedException("无效的refresh-token：缺失唯一标识");
        }

        // 7. 提取iat
        Date iat = Convert.toDate(jwt.getPayload("iat"));
        if (iat == null) {
            throw new UnauthorizedException("无效的" + expectedType + "-token：缺失签发时间");
        }

        return new TokenPayload(userId, jti, iat);
    }

    /**
     * 刷新access-token（通过valid的refresh-token）
     *
     * @param refreshToken 有效的refresh-token
     * @return 新的access-token
     */
    public Map<String, String> refreshAccessToken(String refreshToken, Map<String, Object> extraClaims) {
        String userId = parseRefreshToken(refreshToken); // 先验证refresh-token有效性

        String newAccessToken = createAccessToken(userId, extraClaims);        // 生成新的access-token

        // 刷新 refresh-token（提升安全性，避免长期有效）
        String newRefreshToken = refreshToken;
        try {
            // 计算 refresh-token 剩余有效期
            long remainingTime = jwtProperties.getRefreshTokenTtl().toMillis() - (System.currentTimeMillis() - getRefreshTokenIat(refreshToken).getTime());
            // 剩余有效期小于一半时，生成新的 refresh-token
            if (remainingTime > 0 && remainingTime < (jwtProperties.getRefreshTokenTtl().toMillis() / 2)) {
                // 同时会自动更新 Redis 的refresh-token
                newRefreshToken = createRefreshToken(userId);
                // 使旧 refresh-token 失效
                invalidateRefreshToken(refreshToken);
            }
        } catch (Exception e) {
            // 滚动刷新失败不影响主流程，沿用旧 refresh-token
            log.warn("refresh-token 滚动刷新失败：{}", e.getMessage());
        }

        // 3. 返回新的 Token 对
        Map<String, String> result = new HashMap<>();
        result.put("newAccessToken", newAccessToken);
        result.put("newRefreshToken", newRefreshToken);

        return result;
    }

    /**
     * 注销（使refresh-token失效）,退出登录时调用
     *
     * @param refreshToken 待注销的refresh-token
     */
    public void invalidateRefreshToken(String refreshToken) {
        try {
            // 解析refresh-token获取jti
            JWT jwt = JWT.of(refreshToken).setSigner(jwtSigner);
            String jti = Convert.toStr(jwt.getPayload("jti"));
            if (StrUtil.isNotBlank(jti)) {
                // 从Redis删除
                redis.remove(USER_REFRESH_TOKEN_KEY + jti);
            }
        } catch (Exception e) {
            // 解析失败的Token直接视为无效，无需处理
        }
    }

    /**
     * 提取refresh-token中的iat值
     */
    public Date getRefreshTokenIat(String refreshToken) {
        return parseToken(refreshToken, "refresh").getIat();
    }


    /**
     * 内部类：封装解析后的Token载荷
     */
    @Data
    @AllArgsConstructor
    private static class TokenPayload {
        private final String userId;
        private final String jti;
        private final Date iat;

    }
}