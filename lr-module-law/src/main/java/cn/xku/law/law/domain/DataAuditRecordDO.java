package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 数据审核记录，对应 lr_data_audit_record（平台表 tenant_id=0） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_data_audit_record")
public class DataAuditRecordDO extends BaseDO {

    /** law_import/version_publish/feedback_fix/export_apply */
    private String auditType;
    private String refType;
    private Long refId;
    /** pending/pass/reject/rollback */
    private String auditStatus;
    private Long auditUserId;
    private String auditComment;
    private String beforeJson;
    private String afterJson;
    private LocalDateTime auditTime;
}
