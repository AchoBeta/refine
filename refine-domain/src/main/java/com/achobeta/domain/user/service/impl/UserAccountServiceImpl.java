package com.achobeta.domain.user.service.impl;

import com.achobeta.domain.user.adapter.repository.IUserRepository;
import com.achobeta.domain.user.event.UserLoginEvent;
import com.achobeta.domain.user.model.entity.UserEntity;
import com.achobeta.domain.user.model.valobj.UserLoginVO;
import com.achobeta.domain.user.service.IEmailVerificationService;
import com.achobeta.domain.user.service.IUserAccountService;
import com.achobeta.domain.user.service.Jwt;
import com.achobeta.types.Response;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import com.achobeta.types.support.util.StringTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户领域服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountServiceImpl implements IUserAccountService {

    private final IEmailVerificationService emailVerificationService;

    private final IUserRepository userRepository;

    private final Jwt jwt;
    
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 用户注册（添加验证码验证逻辑）
     *
     * @param email     邮箱
     * @param password  明文密码
     * @param userName  昵称
     * @param checkCode 验证码
     * @return 注册成功
     * @throws AppException 邮箱已注册、验证码无效等异常
     */
    @Override
    public Response register(String email, String password, String userName, String checkCode) {

        // 先验证验证码（核心新增逻辑：调用验证码服务验证）
        emailVerificationService.verifyCode(email, checkCode);

        // 校验邮箱是否已存在
        if (null != userRepository.findByAccount(email)) {
            return Response.CUSTOMIZE_MSG_ERROR(GlobalServiceStatusCode.USER_EMAIL_ALREADY_EXIST,null);
        }

        // 创建用户实体
        UserEntity user = UserEntity.builder()
                .userName(userName)
                .userAccount(email)
                .createTime(LocalDateTime.now())
                .userStatus(1) // 1-正常 0-禁用
                .build();
        user.encryptPassword(password);

        // 5. 保存用户
        userRepository.save(user);
        return Response.SYSTEM_SUCCESS();
    }

    /**
     * 用户登录（保持不变）
     *
     * @param account  邮箱
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
        // 生成token, refresh-token自动写入Redis
        String accessToken = jwt.createAccessToken(user.getUserId(), null);
        String refreshToken = jwt.createRefreshToken(user.getUserId());

        // 发布用户登录事件
        String loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        UserLoginEvent loginEvent = new UserLoginEvent(this, user.getUserId(), loginTime);
        eventPublisher.publishEvent(loginEvent);
        log.info("用户登录事件已发布，userId:{}, loginTime:{}", user.getUserId(), loginTime);

        return UserLoginVO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }

    /**
     * 重置密码
     *
     * @param userAccount  账号
     * @param newPassword 新密码
     * @param checkCode   验证码
     * @return 重置成功
     * @throws AppException 邮箱不存在、验证码无效等异常
     */
    @Override
    public void resetPassword(String userAccount, String newPassword, String checkCode) {
        // 验证该邮箱用户是否存在
        if (null == userRepository.findByAccount(userAccount)) {
            throw new AppException(GlobalServiceStatusCode.USER_ACCOUNT_NOT_EXIST);
        }
        // 验证验证码
        emailVerificationService.verifyCode(userAccount, checkCode);
        // 改密码
        UserEntity user = userRepository.findByAccount(userAccount);
        user.encryptPassword(newPassword);
        userRepository.updateByUserAccount(user, userAccount);
    }

    @Override
    public void updatePassword(String userId, String oldPassword, String newPassword) {
        UserEntity user = userRepository.findById(userId);
        if (!user.verifyPassword(oldPassword)) {
            throw new AppException(GlobalServiceStatusCode.USER_CREDENTIALS_ERROR);
        }
        user.encryptPassword(newPassword);
        userRepository.updateByUserAccount(user, user.getUserAccount());
        logout(jwt.getRefreshToken4UserId(userId));
    }

    @Override
    public void logout(String refreshToken) {
        jwt.invalidateRefreshToken(refreshToken);
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken) {
        return jwt.refreshAccessToken(refreshToken, null);
    }


}