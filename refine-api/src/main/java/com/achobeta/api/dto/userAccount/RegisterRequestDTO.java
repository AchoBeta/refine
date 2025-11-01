package com.achobeta.api.dto.userAccount;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author liangchaowen
 * @description 注册请求DTO
 * @date 2025/10/29
 */
@Data
public class RegisterRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "邮箱或用户名不能为空")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能少于6位")
    private String userPassword;

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "验证码不能为空")
    private String checkCode;//TODO 根据前端字段修改

}