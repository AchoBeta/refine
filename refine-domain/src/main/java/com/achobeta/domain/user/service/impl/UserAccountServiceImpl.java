package com.achobeta.domain.user.service.impl;

import com.achobeta.domain.IRedisService;
import com.achobeta.domain.user.adapter.repository.IUserRepository;
import com.achobeta.domain.user.model.entity.UserEntity;
import com.achobeta.domain.user.model.valobj.UserLoginVO;
import com.achobeta.domain.user.service.IEmailVerificationService;
import com.achobeta.domain.user.service.IUserAccountService;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import com.achobeta.types.support.util.StringTools;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.achobeta.types.common.Constants.*;

/**
 * 用户领域服务实现
 */
@Service
@RequiredArgsConstructor
public class UserAccountServiceImpl implements IUserAccountService {

    private final IEmailVerificationService emailVerificationService;

    private final IUserRepository userRepository;

    private final IRedisService redis;


    /**
     * 用户注册（添加验证码验证逻辑）
     * @param account 邮箱
     * @param password 明文密码
     * @param userName 昵称
     * @param checkCode 验证码
     * @return 注册成功
     * @throws AppException 邮箱已注册、验证码无效等异常
     */
    @Override
    public void register(String account, String password, String userName, String checkCode) {

        if (!StringTools.isEmail(account)) {
            throw new AppException(GlobalServiceStatusCode.USER_EMAIL_FORMAT_ERROR);
        }
        // 先验证验证码（核心新增逻辑：调用验证码服务验证）
        boolean isCodeValid = emailVerificationService.verifyCode(account, checkCode);
        if (!isCodeValid) {
            throw new AppException(GlobalServiceStatusCode.USER_CAPTCHA_CODE_ERROR);
        }

        // 校验邮箱是否已存在
        if (null != userRepository.findByAccount(account)) {
            throw new AppException(GlobalServiceStatusCode.USER_EMAIL_ALREADY_EXIST);
        }

        // 创建用户实体
        UserEntity user = UserEntity.builder()
                .userName(userName)
                .userAccount(account)
                .createTime(LocalDateTime.now())
                .userStatus(1) // 1-正常 0-禁用
                .build();
        user.encryptPassword(password);

        // 5. 保存用户
        userRepository.save(user);
    }

    /**
     * 用户登录（保持不变）
     * @param account 邮箱
     * @param password 明文密码
     * @return 登录成功的用户信息（含令牌）
     * @throws AppException 账号或密码错误/账号禁用等异常
     */
    @Override
    public UserLoginVO login(String account, String password) {
        // 查询用户，校验密码
        UserEntity user = userRepository.findByAccount(account);
        if (null == user || !user.verifyPassword(password)) {
            throw new AppException(GlobalServiceStatusCode.USER_CREDENTIALS_ERROR);
        }
        // 账号状态校验
        if (1 != user.getUserStatus()) {
            throw new AppException(GlobalServiceStatusCode.USER_ACCOUNT_LOCKED);
        }
        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 保存token到缓存
        redis.setValue(USER_ID_KEY_PREFIX + token, user.getUserId(), USER_TOKEN_EXPIRED_SECONDS);

        return UserLoginVO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .token(token)
                .build();

    }

    @Override
    public void logout(String userId) {
        redis.remove(USER_TOKEN_KEY_PREFIX + userId);
        redis.remove(USER_ID_KEY_PREFIX + userId);

    }

}