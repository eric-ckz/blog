package com.eric.blog.service.impl;

import com.eric.blog.common.ErrorCode;
import com.eric.blog.config.MailProperties;
import com.eric.blog.exception.BaseException;
import com.eric.blog.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

/** SMTP 邮件发送实现。未启用时仅记录日志，便于本地开发。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final MailProperties properties;

    @Override
    public void sendVerificationCode(String to, String code) {
        if (!properties.isEnabled()) {
            log.info("[邮件未启用] 注册验证码：邮箱={}, 验证码={}", to, code);
            return;
        }
        try {
            JavaMailSender sender = buildSender();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.getFrom());
            message.setTo(to);
            message.setSubject("Eric Blog 注册验证码");
            message.setText("您的注册验证码是：" + code + "，10 分钟内有效。若非本人操作请忽略。");
            sender.send(message);
        } catch (MailException exception) {
            log.error("验证码邮件发送失败：{}", to, exception);
            throw new BaseException(ErrorCode.OPERATION_ERROR, "验证码邮件发送失败，请稍后重试");
        }
    }

    private JavaMailSender buildSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.getHost());
        sender.setPort(properties.getPort());
        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());
        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if (properties.getPort() == 587) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        } else {
            props.put("mail.smtp.ssl.enable", "true");
        }
        return sender;
    }
}
