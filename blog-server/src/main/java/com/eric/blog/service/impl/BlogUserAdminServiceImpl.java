package com.eric.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.eric.blog.common.ErrorCode;
import com.eric.blog.common.PageResult;
import com.eric.blog.exception.BaseException;
import com.eric.blog.mapper.BlogUserMapper;
import com.eric.blog.model.entity.BlogUser;
import com.eric.blog.model.vo.admin.BlogUserAdminVO;
import com.eric.blog.service.BlogUserAdminService;
import com.eric.blog.utils.ThrowUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/** 管理端访客用户管理实现。 */
@Service
public class BlogUserAdminServiceImpl extends ServiceImpl<BlogUserMapper, BlogUser> implements BlogUserAdminService {

    @Override
    public PageResult<BlogUserAdminVO> listUsers(long page, long pageSize, String keyword) {
        long safePage = Math.max(page, 1);
        long safeSize = Math.min(Math.max(pageSize, 1), 100);
        LambdaQueryWrapper<BlogUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(BlogUser::getEmail, kw)
                    .or().like(BlogUser::getUsername, kw)
                    .or().like(BlogUser::getDisplayName, kw));
        }
        wrapper.orderByDesc(BlogUser::getCreateTime);
        Page<BlogUser> result = page(new Page<>(safePage, safeSize), wrapper);
        List<BlogUserAdminVO> items = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.from(result, items);
    }

    @Override
    public void setUserEnabled(String id, boolean enabled) {
        long userId = parseId(id);
        BlogUser user = getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        user.setEnabled(enabled);
        updateById(user);
    }

    private BlogUserAdminVO toVO(BlogUser user) {
        return BlogUserAdminVO.builder()
                .id(String.valueOf(user.getId()))
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName() == null ? user.getUsername() : user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt() == null ? null : user.getLastLoginAt().toString())
                .createTime(user.getCreateTime() == null ? null : user.getCreateTime().toString())
                .build();
    }

    private long parseId(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "ID 格式不正确");
        }
    }
}
