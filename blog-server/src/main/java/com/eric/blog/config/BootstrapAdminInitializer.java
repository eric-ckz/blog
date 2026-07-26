package com.eric.blog.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eric.blog.mapper.AdminUserMapper;
import com.eric.blog.model.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动管理员初始化器。数据库已有管理员时完全跳过，绝不会因环境变量变化覆盖密码。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;
    private final BootstrapAdminProperties properties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserMapper.selectCount(new LambdaQueryWrapper<>()) > 0) {
            return;
        }
        if (properties.getUsername() == null || properties.getUsername().isBlank()
                || properties.getPassword() == null || properties.getPassword().length() < 10) {
            throw new IllegalStateException("首次启动必须设置 BLOG_ADMIN_USERNAME 和至少 10 位的 BLOG_ADMIN_PASSWORD");
        }
        AdminUser admin = new AdminUser();
        admin.setUsername(properties.getUsername().trim());
        admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        admin.setDisplayName(properties.getDisplayName());
        admin.setEnabled(true);
        adminUserMapper.insert(admin);
        log.info("已创建初始管理员账号：{}", admin.getUsername());
    }
}
