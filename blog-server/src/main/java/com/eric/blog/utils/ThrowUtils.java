package com.eric.blog.utils;

import com.eric.blog.common.ErrorCode;
import com.eric.blog.exception.BaseException;

/** 简化重复业务前置校验的异常工具。 */
public final class ThrowUtils {

    private ThrowUtils() {
    }

    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BaseException(errorCode);
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        if (condition) {
            throw new BaseException(errorCode, message);
        }
    }
}
