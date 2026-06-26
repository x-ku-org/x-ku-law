package cn.xku.law.common.exception;

import lombok.Getter;

/** 统一错误码枚举；业务错误码从 1000 开始按域分段 */
@Getter
public enum ErrorCode {

    // ===== 通用 =====
    SUCCESS(0, "成功"),
    PARAM_ERROR(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    SERVER_ERROR(500, "服务器内部错误"),

    // ===== 认证/鉴权 (1000-1099) =====
    AUTH_USER_NOT_FOUND(1000, "用户不存在"),
    AUTH_PASSWORD_ERROR(1001, "用户名或密码错误"),
    AUTH_TOKEN_INVALID(1002, "Token 无效"),
    AUTH_TOKEN_EXPIRED(1003, "Token 已过期"),
    AUTH_TENANT_DISABLED(1004, "租户已禁用"),
    AUTH_TENANT_NOT_FOUND(1005, "租户不存在"),
    AUTH_USER_DISABLED(1006, "用户已被禁用或锁定"),
    AUTH_USERNAME_EXISTS(1007, "用户名已被注册"),
    AUTH_MOBILE_EXISTS(1008, "手机号已被注册"),

    // ===== 系统 (1100-1199) =====
    SYS_DICT_NOT_FOUND(1100, "字典数据不存在"),
    SYS_CONFIG_NOT_FOUND(1101, "系统配置不存在"),
    SYS_FILE_UPLOAD_FAIL(1102, "文件上传失败"),
    SYS_FILE_NOT_FOUND(1103, "文件记录不存在"),
    SYS_FILE_TYPE_NOT_ALLOWED(1104, "不支持的文件类型"),
    SYS_FILE_TOO_LARGE(1105, "文件大小超出限制"),
    SYS_FILE_NOT_UPLOADED(1106, "对象不存在或上传未完成"),

    // ===== 法规 (2000-2099) =====
    LAW_DOCUMENT_NOT_FOUND(2000, "法规文件不存在"),
    LAW_VERSION_NOT_FOUND(2001, "法规版本不存在"),
    LAW_UID_DUPLICATE(2002, "法规唯一标识已存在"),
    LAW_ARTICLE_NOT_FOUND(2003, "法规条款不存在"),
    LAW_CATEGORY_NOT_FOUND(2004, "法规分类不存在"),
    LAW_RELATION_NOT_FOUND(2005, "法规关系不存在"),
    LAW_VERSION_ALREADY_PUBLISHED(2006, "法规版本已发布，不可重复发布"),

    // ===== AI (3000-3099) =====
    AI_SESSION_NOT_FOUND(3000, "AI 会话不存在"),
    AI_MODEL_UNAVAILABLE(3001, "AI 模型服务不可用"),

    // ===== 合规 (4000-4099) =====
    COMPLIANCE_SUBJECT_NOT_FOUND(4000, "合规主体不存在"),
    COMPLIANCE_LIST_NOT_FOUND(4001, "合规清单不存在"),

    // ===== 订阅/检索 (5000-5099) =====
    SUBSCRIPTION_RULE_NOT_FOUND(5000, "订阅规则不存在"),
    SAVED_SEARCH_NOT_FOUND(5001, "保存检索不存在");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
