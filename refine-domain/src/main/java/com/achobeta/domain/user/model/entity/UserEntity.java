package com.achobeta.domain.user.model.entity;

import com.achobeta.types.exception.AppException;
import lombok.*;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author liangchaowen
 * @description Render Domain 用户登录领域的用户实体对象
 * @create 2025/10/29
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 昵称
     */
    private String userName;
    /**
     * 头像
     */
    private String userPictureResource;
    /**
     * 用户账号（登录账号）
     */
    private String userAccount;
    /**
     * 邮箱（其一）
     */
    private String userEmail;
    /**
     * 手机号（其一）
     */
    private String userPhoneNum;
    /**
     * 加密后的密码
     */
    private String userPassword;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 用户账号状态
     */
    private int userStatus;


    /**
     * 密码加密（注册时调用，封装业务规则）
     */
    public void encryptPassword(String rawPassword) {
        // 密码不能为空
        if (null == rawPassword) {
            throw new AppException("密码不能为空");
        }
        // 返回加密后的字符串（含自动生成的盐值）
        this.userPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt(12));
        // 12是工作因子，越大越安全但耗时越长，推荐10-14
    }

    /**
     * 密码校验（登录时调用，核心业务逻辑）
     */
    public boolean verifyPassword(String inputPassword) {
        // 校验输入密码和存储的加密密码非空
        if (null == inputPassword || null == userPassword) {
            return false;
        }
        // BCrypt.checkpw：自动提取加密字符串中的盐值，与输入密码比对
        return BCrypt.checkpw(inputPassword, userPassword);
    }

}