package cn.xku.law.common.result;

import cn.xku.law.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/** 统一 HTTP 响应体，所有 Controller 均通过此类返回 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResult<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    private CommonResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(0, "success", data);
    }

    public static <T> CommonResult<T> success() {
        return new CommonResult<>(0, "success", null);
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return new CommonResult<>(errorCode.getCode(), errorCode.getMsg(), null);
    }

    public static <T> CommonResult<T> error(int code, String msg) {
        return new CommonResult<>(code, msg, null);
    }

    public boolean isSuccess() {
        return this.code != null && this.code == 0;
    }
}
