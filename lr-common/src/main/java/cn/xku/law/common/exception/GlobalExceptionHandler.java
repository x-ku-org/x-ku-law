package cn.xku.law.common.exception;

import cn.xku.law.common.result.CommonResult;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/** 全局异常拦截器，将各类异常统一转为 CommonResult 返回 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
            Map.entry("tenantCode", "租户编码"),
            Map.entry("username", "用户名"),
            Map.entry("password", "密码"),
            Map.entry("title", "标题"),
            Map.entry("content", "内容"),
            Map.entry("dictCode", "字典编码"),
            Map.entry("dictName", "字典名称"),
            Map.entry("dictLabel", "字典标签"),
            Map.entry("dictValue", "字典值"),
            Map.entry("roleCode", "角色编码"),
            Map.entry("roleName", "角色名称"),
            Map.entry("permissionCode", "权限编码"),
            Map.entry("permissionName", "权限名称"),
            Map.entry("documentId", "法规标识"),
            Map.entry("question", "问题")
    );

    @ExceptionHandler(SearchUnavailableException.class)
    public ResponseEntity<CommonResult<?>> handleSearchUnavailable(SearchUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(CommonResult.error(503, ex.getMessage()));
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<CommonResult<?>> handleAppException(AppException ex) {
        return ResponseEntity.status(resolveHttpStatus(ex.getCode()))
                .body(CommonResult.error(ex.getCode(), ex.getMsg()));
    }

    private HttpStatus resolveHttpStatus(int code) {
        HttpStatus explicitStatus = HttpStatus.resolve(code);
        if (explicitStatus != null) {
            return explicitStatus;
        }
        return switch (code) {
            case 1000, 1005, 1100, 1101, 1103, 2000, 2001, 2003, 2004, 2005, 3000, 4000, 4001, 5000, 5001 ->
                    HttpStatus.NOT_FOUND;
            case 1001, 1002, 1003 -> HttpStatus.UNAUTHORIZED;
            case 1004, 1006 -> HttpStatus.FORBIDDEN;
            case 1007, 1008, 2002, 2006 -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    /** Bean Validation（@RequestBody + @Valid）失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> formatValidationMessage(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.joining("；"));
        return CommonResult.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    /** @Validated 在 @RequestParam / @PathVariable 上失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<?> handleConstraintViolation(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> formatValidationMessage(lastPathSegment(v.getPropertyPath().toString()), v.getMessage()))
                .collect(Collectors.joining("；"));
        return CommonResult.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public CommonResult<?> handleBindException(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> formatValidationMessage(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.joining("；"));
        return CommonResult.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    private static String formatValidationMessage(String field, String message) {
        if (message != null && containsChinese(message)) {
            return message;
        }
        if (message != null && isDefaultBlankMessage(message)) {
            return fieldLabel(field) + "不能为空";
        }
        if (message == null || message.isBlank()) {
            return fieldLabel(field) + "不能为空";
        }
        return fieldLabel(field) + "：" + message;
    }

    private static boolean isDefaultBlankMessage(String message) {
        return "must not be blank".equalsIgnoreCase(message)
                || "must not be null".equalsIgnoreCase(message)
                || "must not be empty".equalsIgnoreCase(message);
    }

    private static boolean containsChinese(String value) {
        return value.chars().anyMatch(ch -> ch >= 0x4E00 && ch <= 0x9FFF);
    }

    private static String fieldLabel(String field) {
        return FIELD_LABELS.getOrDefault(field, field);
    }

    private static String lastPathSegment(String propertyPath) {
        int dot = propertyPath.lastIndexOf('.');
        return dot >= 0 ? propertyPath.substring(dot + 1) : propertyPath;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public CommonResult<?> handleMissingParam(MissingServletRequestParameterException ex) {
        return CommonResult.error(ErrorCode.PARAM_ERROR.getCode(), "缺少必填参数：" + ex.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<?> handleNotReadable(HttpMessageNotReadableException ex) {
        return CommonResult.error(ErrorCode.PARAM_ERROR.getCode(), "请求体格式错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return CommonResult.error(ErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public CommonResult<?> handleAll(Exception ex) {
        log.error("[GlobalExceptionHandler] unexpected error", ex);
        return CommonResult.error(ErrorCode.SERVER_ERROR);
    }
}
