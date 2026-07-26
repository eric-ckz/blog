package com.eric.blog.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 统一业务错误码。业务码用于前端稳定判断，HTTP 状态码用于代理、监控和浏览器
 * 正确理解请求结果。
 */
@Getter
public enum ErrorCode {
    SUCCESS("20000", "ok", HttpStatus.OK),
    PARAMS_ERROR("40000", "请求参数错误", HttpStatus.BAD_REQUEST),
    NOT_LOGIN_ERROR("40100", "未登录或登录已过期", HttpStatus.UNAUTHORIZED),
    NO_AUTH_ERROR("40300", "没有操作权限", HttpStatus.FORBIDDEN),
    NOT_FOUND_ERROR("40400", "请求数据不存在", HttpStatus.NOT_FOUND),
    CONFLICT_ERROR("40900", "数据状态冲突", HttpStatus.CONFLICT),
    FILE_ERROR("42200", "文件不符合要求", HttpStatus.UNPROCESSABLE_ENTITY),
    OPERATION_ERROR("50001", "操作失败", HttpStatus.INTERNAL_SERVER_ERROR),
    SYSTEM_ERROR("50000", "系统内部异常", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
