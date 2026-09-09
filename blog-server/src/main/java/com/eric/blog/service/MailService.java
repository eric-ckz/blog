package com.eric.blog.service;

/** 邮件发送服务。 */
public interface MailService {

    /** 发送注册验证码邮件。 */
    void sendVerificationCode(String to, String code);
}
