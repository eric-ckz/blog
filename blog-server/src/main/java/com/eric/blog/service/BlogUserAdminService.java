package com.eric.blog.service;

import com.eric.blog.common.PageResult;
import com.eric.blog.model.vo.admin.BlogUserAdminVO;

/** 管理端访客用户管理服务。 */
public interface BlogUserAdminService {
    PageResult<BlogUserAdminVO> listUsers(long page, long pageSize, String keyword);
    void setUserEnabled(String id, boolean enabled);
}
