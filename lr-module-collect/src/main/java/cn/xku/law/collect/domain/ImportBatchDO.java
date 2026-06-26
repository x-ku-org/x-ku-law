package cn.xku.law.collect.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 导入批次，对应 lr_import_batch。一个运行文件夹对应一个批次。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_import_batch")
public class ImportBatchDO extends BaseDO {

    /** 批次号，唯一（uk_import_batch_no） */
    private String batchNo;
    private Long sourceId;
    /** manual/collect/api */
    private String importType;
    private Long fileId;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    /** pending/processing/success/partial/failed */
    private String status;
    /** pending/approved/rejected */
    private String auditStatus;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
