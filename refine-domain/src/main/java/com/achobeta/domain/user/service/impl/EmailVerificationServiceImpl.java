package com.achobeta.domain.user.service.impl;

import com.achobeta.domain.IRedisService;
import com.achobeta.domain.user.adapter.repository.IUserRepository;
import com.achobeta.domain.user.service.IEmailVerificationService;
import com.achobeta.types.enums.GlobalServiceStatusCode;
import com.achobeta.types.exception.AppException;
import com.achobeta.types.support.util.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.achobeta.types.common.Constants.*;

/**
 * 邮箱验证码领域服务实现
 * 依赖基础设施层的 Redis和 JavaMailSender（邮件发送）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements IEmailVerificationService {

    private final JavaMailSender mailSender;

    private final IRedisService redis;

    @Value("${email.config.verify-code-expire-minute}")
    private int CODE_EXPIRE_MINUTES; // 验证码有效期,minutes

    @Value("${spring.mail.username}")
    private String SENDER_EMAIL;


    /**
     * 发送注册验证码
     */
    @Override
    public void sendEmailCode(String userEmail) {
        // 校验邮箱格式
        if (!StringTools.isEmail(userEmail)) {
            throw new AppException(GlobalServiceStatusCode.USER_CAPTCHA_CODE_ERROR);
        }

        // 检查发送频率
        String sendRecordKey = SEND_RECORD_PREFIX + userEmail;
        String lastSendTime = redis.getValue(sendRecordKey);
        if (lastSendTime != null) {
            LocalDateTime lastTime = LocalDateTime.parse(lastSendTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            if (Duration.between(lastTime, LocalDateTime.now()).getSeconds() < SEND_INTERVAL_SECONDS) {
                throw new AppException("发送过于频繁，请1分钟后重试");
            }
        }

        // 验证码
        String code = generateSecureCode();

        // 缓存验证码（设置过期时间，与有效期一致）
        String codeKey = CACHE_KEY_PREFIX + userEmail;
        redis.setValue(codeKey, code, CODE_EXPIRE_MINUTES * 60L);

        // 记录发送时间（用于频率限制）
        redis.setValue(
            sendRecordKey,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            SEND_INTERVAL_SECONDS
        );

        // 发送验证码邮件
        try {
            sendVerificationEmail(userEmail, code);
        } catch (MessagingException e) {
            // 发送失败时清理缓存,避免验证码残留
            redis.remove(codeKey);
            throw new AppException("验证码发送失败，请稍后重试");
        }
    }


    /**
     * 验证注册验证码
     */
    @Override
    public boolean verifyCode(String userEmail, String checkCode) {
        // 1. 校验邮箱格式（二次校验，避免无效请求）
        if (!StringTools.isEmail(userEmail)) {
            throw new AppException("无效的邮箱格式");
        }

        // 2. 从缓存获取验证码
        String codeKey = CACHE_KEY_PREFIX + userEmail;
        String cachedCode = redis.getValue(codeKey);

        // 3. 验证验证码
        if (null == cachedCode) {
            throw new AppException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equals(checkCode)) {
            throw new AppException("验证码错误");
        }

        // 验证通过后删除缓存（避免重复使用）
        redis.remove(codeKey);
        return true;
    }


    // 6位随机验证码
    private String generateSecureCode() {
        SecureRandom random = new SecureRandom();
        int codeNum = 100000 + random.nextInt(900000); // 6位数字（100000-999999）
        return String.valueOf(codeNum);
    }


    // 发送验证码邮件（基础设施层操作：封装邮件发送细节）
    private void sendVerificationEmail(String toEmail, String code) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), true);
        helper.setFrom(SENDER_EMAIL); // 发件人
        helper.setTo(toEmail); // 收件人
        helper.setSubject("注册验证码"); // 邮件主题
        // 邮件内容（HTML格式，清晰提示有效期）
        String content = String.format(
            "<div style='font-size:14px'>" +
            "您好，您正在注册错题整理系统，验证码为：<br>" +
            "<div style='font-size:20px;color:#ff4d4f;font-weight:bold;margin:10px 0'>%s</div>" +
            "验证码%d分钟内有效，请勿泄露给他人。<br>" +
            "如果不是您本人操作，请忽略此邮件。" +
            "</div>",
            code, CODE_EXPIRE_MINUTES
        );
        helper.setText(content, true); // 支持HTML

        mailSender.send(helper.getMimeMessage()); // 发送邮件

        log.info("验证码邮件已发送至：{}", toEmail);
    }
}