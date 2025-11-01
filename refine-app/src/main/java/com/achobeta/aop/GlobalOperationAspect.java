package com.achobeta.aop;

import com.achobeta.domain.IRedisService;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.Constants;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import com.achobeta.types.support.util.StringTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component("globalOperationAspect")
@Slf4j
@RequiredArgsConstructor
public class GlobalOperationAspect {

    private final IRedisService redis;

    /**
     * 拦截被@GlobalInterception注解标记的方法，执行登录检查
     */
    @Before("@annotation(com.achobeta.types.annotation.GlobalInterception)")
    public void interceptorDo(JoinPoint point) {
        try {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            GlobalInterception interceptor = method.getAnnotation(GlobalInterception.class);
            if (interceptor == null) {
                return;
            }
            if (interceptor.checkLogin()) {
                checkLogin();
            }
        } catch (AppException e) {
            log.error("全局拦截异常", e);
            throw e;
        } catch (Throwable e) {
            log.error("全局拦截异常", e);
            throw new AppException(GlobalServiceStatusCode.PARAM_FAILED_VALIDATE);
        }
    }

    /**
     * 核心登录检查逻辑：验证token有效性
     */
    private void checkLogin() {
        // 获取当前请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 从请求头获取token
        String token = request.getHeader("token");
        if (StringTools.isEmpty(token)) {
            throw new AppException(GlobalServiceStatusCode.USER_NOT_LOGIN); // token为空，未登录
        }

        // 从Redis查询token对应的用户信息
        String redisKey = Constants.USER_ID_KEY_PREFIX + token;
        String userId = redis.getValue(redisKey);

        if (null == userId) {
            throw new AppException(GlobalServiceStatusCode.USER_NOT_LOGIN); // token无效或已过期，未登录
        }
    }

}