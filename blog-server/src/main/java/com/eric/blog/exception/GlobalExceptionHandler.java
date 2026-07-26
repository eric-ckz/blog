package com.eric.blog.exception;

import com.eric.blog.common.BaseResponse;
import com.eric.blog.common.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * 全局异常出口。Controller 不捕获业务异常，保证所有失败响应具有相同格式，
 * 同时避免把堆栈和数据库细节泄露给浏览器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(BaseException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(BaseResponse.error(errorCode, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return badRequest(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return badRequest(exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(ErrorCode.NO_AUTH_ERROR.getHttpStatus())
                .body(BaseResponse.error(ErrorCode.NO_AUTH_ERROR, ErrorCode.NO_AUTH_ERROR.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseResponse<Void>> handleMaxUpload(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(ErrorCode.FILE_ERROR.getHttpStatus())
                .body(BaseResponse.error(ErrorCode.FILE_ERROR, "图片大小不能超过 10MB"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnknown(Exception exception) {
        log.error("未处理的系统异常", exception);
        return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
                .body(BaseResponse.error(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage()));
    }

    private ResponseEntity<BaseResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest().body(BaseResponse.error(ErrorCode.PARAMS_ERROR, message));
    }
}
