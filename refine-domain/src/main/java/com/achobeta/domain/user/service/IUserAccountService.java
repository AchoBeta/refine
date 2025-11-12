package com.achobeta.domain.user.service;

import com.achobeta.domain.user.model.valobj.UserLoginVO;
import com.achobeta.types.exception.AppException;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * @author liangchaowen
 * @description 用户领域服务接口
 * @date 2025/10/29
 */
public interface IUserAccountService {

    /**
     * 用户注册
     *
     * @param userEmail    邮箱
     * @param userPassword 明文密码（领域层负责加密）
     * @param userName     昵称
     * @param checkCode    验证码
     * @return 注册成功
     * @throws AppException 邮箱已注册等异常
     */
    void register(String userEmail, String userPassword, String userName, String checkCode);

    /**
     * 用户登录
     *
     * @param userEmail    邮箱
     * @param userPassword 明文密码
     * @return 登录成功的用户信息（含令牌）
     * @throws AppException 账号或密码错误/账号禁用等异常
     */
    UserLoginVO login(String userEmail, String userPassword);

    void resetPassword(String userEmail, String newPassword, String checkCode);

    void updatePassword(String userId, String oldPassword, String newPassword);

    void logout(String refreshToken);

    Map<String, String> refreshToken(String refreshToken);
}