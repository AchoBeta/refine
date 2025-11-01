package com.achobeta.types.exception;

import com.achobeta.types.enums.GlobalServiceStatusCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AppException extends RuntimeException {

    private static final long serialVersionUID = 5317680961212299217L;

    /** 返回码(GlobalServiceStatusCode枚举类里的) */
    GlobalServiceStatusCode statusCode;

    /** 异常码（子类自定义字段） */
    private Integer code;

    /** 异常消息（子类自定义字段） */
    private String message;


    // 仅传异常消息
    public AppException(String message) {
        super(message);
        this.code = null;
        this.message = message; // 同时初始化子类自己的message
    }

    // 异常码 + 异常原因
    public AppException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
        this.message = null;
    }

    // 异常码 + 异常消息
    public AppException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    // 异常码 + 异常消息 + 原因
    public AppException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public AppException(GlobalServiceStatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
        this.code = statusCode.getCode();
        this.message = statusCode.getMessage();

    }

    @Override
    public String toString() {
        return "com.achobeta.x.api.types.exception.XApiException{" +
                "code='" + code + '\'' +
                ", info='" + message + '\'' +
                '}';
    }

}