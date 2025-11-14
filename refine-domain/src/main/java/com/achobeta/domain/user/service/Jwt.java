package com.achobeta.domain.user.service;

import java.util.Date;
import java.util.Map;

public interface Jwt {

    String createAccessToken(String userId, Map<String, Object> extraClaims);

    String createRefreshToken(String userId);

    String parseAccessToken(String token);

    String parseRefreshToken(String token);

    Map<String, String> refreshAccessToken(String refreshToken, Map<String, Object> extraClaims);

    void invalidateRefreshToken(String refreshToken);

    Date getRefreshTokenIat(String refreshToken);

}
