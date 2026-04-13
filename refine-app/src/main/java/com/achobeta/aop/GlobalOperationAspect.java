package com.achobeta.aop;

import com.achobeta.jwt.JwtTool;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import com.achobeta.types.exception.UnauthorizedException;
import com.achobeta.types.support.util.StringTools;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component("globalOperationAspect")
@Slf4j
@RequiredArgsConstructor
public class GlobalOperationAspect {

    private final JwtTool jwtTool;

    /**
     * 拦截被@GlobalInterception注解标记的方法，执行登录检查
     */
    @Before("@annotation(com.achobeta.types.annotation.GlobalInterception)")
    public void interceptorDo(JoinPoint point) {
        try {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            GlobalInterception interceptor = method.getAnnotation(GlobalInterception.class);
            if (interceptor == null || !interceptor.checkLogin()) {
                return;
            }
            checkLogin();
        } catch (UnauthorizedException e) {
            log.error("登录验证失败：{}", e.getMessage());
            throw new AppException(e.getCode(), e.getMessage());
        } catch (AppException e) {
            log.error("全局拦截异常", e);
            throw new AppException(e.getCode(), e.getMessage());
        } catch (Throwable e) {
            log.error("全局拦截异常", e);
            throw new AppException(GlobalServiceStatusCode.PARAM_FAILED_VALIDATE);
        }
    }


    @After("@annotation(com.achobeta.types.annotation.GlobalInterception)")
    public void afterCompletion(JoinPoint point) {
        UserContext.clear();
    }


    /**
     * 核心登录检查逻辑：验证token有效性
     */
    private void checkLogin() {
        // 获取当前请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AppException("无法获取请求上下文");
        }
        HttpServletRequest request = attributes.getRequest();

        // 从请求头获取token
        String token = request.getHeader("access-token");
        if (StringTools.isEmpty(token)) {
            throw new AppException(401, "token为空"); // token为空，未登录
        }

        // 先JWT技术验证，验签名、过期时间
        String userId;
        try {
            // JwtTool 内部校验签名和过期
            userId = jwtTool.parseAccessToken(token);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException(e.getMessage());
        }
        // 存入上下文
        UserContext.setUserId(userId);
    }

}
