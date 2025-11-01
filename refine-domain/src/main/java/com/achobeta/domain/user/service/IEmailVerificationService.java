package com.achobeta.domain.user.service;

import com.achobeta.types.exception.AppException;

/**
 * 邮箱验证码领域服务（专注于验证码的生成、发送、验证）
 */
public interface IEmailVerificationService {

    /**
     * 发送注册验证码
     * @param userEmail 目标邮箱
     * @throws AppException 邮箱格式错误、发送频繁等异常
     */
    void sendEmailCode(String userEmail);

    /**
     * 验证注册验证码
     * @param userEmail 邮箱
     * @param checkCode 输入的验证码
     * @return 验证通过返回true
     * @throws AppException 验证码无效/过期等异常
     */
    boolean verifyCode(String userEmail, String checkCode);
}