package com.achobeta.trigger.http;

import com.achobeta.api.dto.userAccount.LoginRequestDTO;
import com.achobeta.api.dto.userAccount.RegisterRequestDTO;
import com.achobeta.domain.user.model.valobj.UserLoginVO;
import com.achobeta.domain.user.service.IEmailVerificationService;
import com.achobeta.domain.user.service.IUserAccountService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.Constants;
import com.achobeta.types.common.UserContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author liangchaowen
 * @date 2025/10/29
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/userAccount")
@RequiredArgsConstructor
public class UserAccountController {

    private final IEmailVerificationService emailVerificationService;

    private final IUserAccountService userAccountService;


    /**
     * 发送邮箱验证码
     *
     * @param userAccount 邮箱
     * @return 发送成功
     */
    @PostMapping("/sendEmailCode")
    public Response sendEmailCode(@NotBlank(message = "接收验证码邮箱不能为空") String userAccount) {
        emailVerificationService.sendEmailCode(userAccount);
        return Response.SYSTEM_SUCCESS();
    }

    @PostMapping("/register")
    public Response register(@RequestBody RegisterRequestDTO request) {
        userAccountService.register(request.getUserAccount(), request.getUserPassword(), request.getUserName(), request.getCheckCode());
        return Response.SYSTEM_SUCCESS();
    }

    @PostMapping("/login")
    public Response<UserLoginVO> login(@RequestBody LoginRequestDTO request) {
        UserLoginVO user = userAccountService.login(request.getUserAccount(), request.getUserPassword());
        log.info("用户 {} 登录", request.getUserAccount());
        return Response.SYSTEM_SUCCESS(user);
    }

    @GlobalInterception
    @PostMapping("/logout")
    public Response logout(@RequestHeader("refresh-token") String refreshToken) {
        userAccountService.logout(refreshToken);
        log.info("用户 {} 登出", UserContext.getUserId());
        return Response.SYSTEM_SUCCESS();
    }

    /**
     * 重置密码（忘记密码）
     */
    @PostMapping("/resetPassword")
    @Validated
    public Response resetPassword(@NotBlank String userEmail, @NotBlank @Pattern(regexp = Constants.REGEX_PASSWORD) String newPassword, @NotBlank String checkCode) {
        userAccountService.resetPassword(userEmail, newPassword, checkCode);
        log.info("账号 {} 重置密码", userEmail);
        return Response.SYSTEM_SUCCESS();
    }

    /**
     * 修改密码
     */
    @GlobalInterception
    @PostMapping("/updatePassword")
    public Response updatePassword(@NotBlank String oldPassword, @NotBlank @Pattern(regexp = Constants.REGEX_PASSWORD) String newPassword) {
        String userId = UserContext.getUserId();
        userAccountService.updatePassword(userId, oldPassword, newPassword);
        log.info("userId {} 修改密码", userId);
        return Response.SYSTEM_SUCCESS();
    }


    @PostMapping("/refreshToken")
    public Response<Map<String, String>> refreshToken(@RequestHeader("refresh-token") String refreshToken) {
        Map<String, String> newToken = userAccountService.refreshToken(refreshToken);
        return Response.SYSTEM_SUCCESS(newToken);
    }

}