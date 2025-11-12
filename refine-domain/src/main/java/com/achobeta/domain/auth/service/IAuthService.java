package com.achobeta.domain.auth.service;

/**
 * @Auth : Malog
 * @Desc : 认证
 * @Time : 2025/11/05 17:29
 */
public interface IAuthService {

    boolean checkToken(String token);

    String openid(String token);

}
