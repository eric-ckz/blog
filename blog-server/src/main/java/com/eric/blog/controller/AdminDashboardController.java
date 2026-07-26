package com.eric.blog.controller;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.model.vo.admin.DashboardVO;
import com.eric.blog.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 管理端首页统计接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public BaseResponse<DashboardVO> dashboard() {
        return BaseResponse.success(dashboardService.getDashboard());
    }
}
