package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 操作日志，对应 lr_operation_log */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_operation_log")
public class OperationLogDO extends BaseDO {

    private Long userId;
    private String moduleName;
    /** create/update/delete/export/audit */
    private String operationType;
    private String requestMethod;
    private String requestUrl;
    private String requestParams;
    /** success/fail */
    private String responseStatus;
    private String ip;
    private Integer durationMs;
    private LocalDateTime operationTime;
}
