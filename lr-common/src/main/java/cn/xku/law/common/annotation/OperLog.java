package cn.xku.law.common.annotation;

import java.lang.annotation.*;

/** 操作日志注解，标注在写操作 Controller 方法上，由 OperLogAspect 拦截并写入 lr_operation_log */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 模块名称，如「用户管理」「法规管理」 */
    String module();

    /** 操作类型：create/update/delete/export/audit */
    String type();
}
