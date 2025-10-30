package com.achobeta.infrastructure.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @Auth : Malog
 * @Desc : 用户基础信息
 * @Time : 2025/10/30 17:22
 */
@Data
public class UserInformation {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private String userId;

    /** 用户名 */
    private String userName;

    /** 用户头像资源 */
    private String userPictureResource;

    /** 用户账号 */
    private String userAccount;

    /** 用户手机号 */
    private String userPhoneNum;

    /** 用户邮箱 */
    private String userEmail;

    /** 用户密码 */
    private String userPassword;

    /** 创建时间 */
    private Date createTime;

    /** 用户状态 */
    private int status;


}
