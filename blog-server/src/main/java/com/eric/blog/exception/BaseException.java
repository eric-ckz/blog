package com.eric.blog.exception;

import com.eric.blog.common.ErrorCode;
import lombok.Getter;

/** 可预期的业务异常，由全局异常处理器转换为统一响应。 */
@Getter
public class BaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage());
    }

    public BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
