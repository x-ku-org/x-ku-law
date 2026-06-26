package cn.xku.law.common.exception;

import lombok.Getter;

/** 业务异常，由 GlobalExceptionHandler 统一处理并映射到 CommonResult */
@Getter
public class AppException extends RuntimeException {

    private final int code;
    private final String msg;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.msg = errorCode.getMsg();
    }

    public AppException(ErrorCode errorCode, String detail) {
        super(detail);
        this.code = errorCode.getCode();
        this.msg = detail;
    }

    public AppException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
