package com.achobeta.api.dto.userAccount;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * @author liangchaowen
 * @description 登录请求DTO
 * @date 2025/10/29
 */
@Data
public class LoginRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "邮箱或手机号不能为空")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    private String userPassword;
}