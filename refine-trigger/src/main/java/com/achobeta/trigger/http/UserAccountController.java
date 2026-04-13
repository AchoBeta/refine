package com.achobeta.trigger.http;

import cn.hutool.core.date.DateTime;
import com.achobeta.api.dto.LoginRequestDTO;
import com.achobeta.api.dto.RegisterRequestDTO;
import com.achobeta.domain.overview.service.extendbiz.UserOverviewService;
import com.achobeta.domain.user.model.valobj.UserLoginVO;
import com.achobeta.domain.user.service.IEmailVerificationService;
import com.achobeta.domain.user.service.IUserAccountService;
import com.achobeta.types.Response;
import com.achobeta.types.annotation.GlobalInterception;
import com.achobeta.types.common.Constants;
import com.achobeta.types.common.UserContext;
import com.achobeta.types.exception.AppException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final UserOverviewService userOverviewService;
    private final Map<String, DateTime> sessionMap = new ConcurrentHashMap<>();


    /**
     * 发送邮箱验证码
     *
     * @param userAccount 邮箱
     * @return 发送成功
     */
    @PostMapping("/sendEmailCode")
    public Response sendEmailCode(@NotBlank(message = "接收验证码邮箱不能为空") String userAccount) {
        try {
            emailVerificationService.sendEmailCode(userAccount);
        } catch (AppException e) {
            return Response.CUSTOMIZE_MSG_ERROR(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return Response.SYSTEM_SUCCESS();
    }

    @PostMapping("/register")
    public Response register(@RequestBody RegisterRequestDTO request) {
        try {
            return userAccountService.register(request.getUserAccount(), request.getUserPassword(), request.getUserName(), request.getCheckCode());
        } catch (AppException e) {
            return Response.CUSTOMIZE_MSG_ERROR(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Response<UserLoginVO> login(@RequestBody LoginRequestDTO request) {
        UserLoginVO user = null;
        try {
            user = userAccountService.login(request.getUserAccount(), request.getUserPassword());
            log.info("用户 {} 登录", request.getUserAccount());
            sessionMap.put(user.getUserId(), DateTime.now());
        } catch (AppException e) {
            return Response.CUSTOMIZE_MSG_ERROR(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return Response.SYSTEM_SUCCESS(user);
    }

    @GlobalInterception
    @PostMapping("/logout")
    public Response logout(@RequestHeader("refresh-token") String refreshToken) {
        try {
            userAccountService.logout(refreshToken);
            log.info("用户 {} 登出", UserContext.getUserId());
            int duration = (int) (DateTime.now().getTime() - sessionMap.get(UserContext.getUserId()).getTime()) / 3600;
            userOverviewService.updateUserDuration(UserContext.getUserId(), duration);
            sessionMap.remove(UserContext.getUserId());
        } catch (AppException e) {
            return Response.CUSTOMIZE_MSG_ERROR(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return Response.SYSTEM_SUCCESS();
    }

    /**
     * 重置密码（忘记密码）
     */
    @PostMapping("/resetPassword")
    public Response resetPassword(@NotBlank String userAccount, @NotBlank @Pattern(regexp = Constants.REGEX_PASSWORD) String newPassword, @NotBlank String checkCode) {
        try {
            userAccountService.resetPassword(userAccount, newPassword, checkCode);
            log.info("账号 {} 重置密码", userAccount);
        } catch (AppException e) {
            return Response.CUSTOMIZE_MSG_ERROR(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return Response.SYSTEM_SUCCESS();
    }

    /**
     * 修改密码
     */
    @GlobalInterception
    @PostMapping("/updatePassword")
    public Response updatePassword(@NotBlank String oldPassword, @NotBlank @Pattern(regexp = Constants.REGEX_PASSWORD) String newPassword) {
        String userId = UserContext.getUserId();
        try {
            userAccountService.updatePassword(userId, oldPassword, newPassword);
            log.info("userId {} 修改密码", userId);
            return Response.SYSTEM_SUCCESS("修改密码成功，请重新登录");
        } catch (AppException e) {
            return Response.CUSTOMIZE_MSG_ERROR(e.getCode(), e.getMessage(), null);
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }


    @PostMapping("/refreshToken")
    public Response<Map<String, String>> refreshToken(@RequestHeader("refresh-token") String refreshToken) {
        Map<String, String> newToken;
        try {
            newToken = userAccountService.refreshToken(refreshToken);
            if(sessionMap.get(UserContext.getUserId()) == null){
                // 这里异常状态其实用心跳机制或websocket比较好，但是服务器不知道能不能行，因此默认1小时
                userOverviewService.updateUserDuration(UserContext.getUserId(), 1);
                sessionMap.put(UserContext.getUserId(), DateTime.now());
            }else{
                // 其实时长应该double比较合适，改换为int可能学几天也未必有1小时
                int duration = (int) (DateTime.now().getTime() - sessionMap.get(UserContext.getUserId()).getTime()) / (1000 * 3600);
                userOverviewService.updateUserDuration(UserContext.getUserId(), duration);
                sessionMap.put(UserContext.getUserId(), DateTime.now());
            }
            log.info("用户刷新access-token");
            return Response.SYSTEM_SUCCESS(newToken);
        } catch (AppException e) {
            return Response.CUSTOMIZE_MSG_ERROR(e.getCode(), e.getMessage(), null);
        }

    }

}