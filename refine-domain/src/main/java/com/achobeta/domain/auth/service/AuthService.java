package com.achobeta.domain.auth.service;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Auth : Malog
 * @Desc : 鉴权认证
 * @Time : 2025/11/06 17:27
 */
@Slf4j
@Service
public class AuthService extends AbstractAuthService {

    @Override
    public boolean checkToken(String token) {
        return isVerify(token);
    }

    @Override
    public String openid(String token) {
        Claims claims = decode(token);
        return claims.get("openId").toString();
    }

}
