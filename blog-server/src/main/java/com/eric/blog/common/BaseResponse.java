package com.eric.blog.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 前后端统一响应模型。
 *
 * @param <T> 实际业务数据类型
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class BaseResponse<T> {

    /** 业务状态码，字符串类型兼容现有用户端契约。 */
    private String code;

    /** 面向调用方的简短结果说明。 */
    private String message;

    /** 实际响应数据；失败时通常为 null。 */
    private T data;

    /** 创建成功响应。 */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /** 创建没有响应体数据的成功响应。 */
    public static BaseResponse<Void> success() {
        return success(null);
    }

    /** 根据标准错误码创建失败响应。 */
    public static BaseResponse<Void> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), message, null);
    }
}
