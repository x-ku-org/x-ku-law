package cn.xku.law.collect.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 导入明细，对应 lr_import_record。一条元数据记录对应一行。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_import_record")
public class ImportRecordDO extends BaseDO {

    private Long batchId;
    private Long sourceId;
    private Long rawDocumentId;
    private Long lawDocumentId;
    private Long lawVersionId;
    /** pending/success/failed */
    private String recordStatus;
    private String errorMessage;
    /** 是否疑似重复（已存在同 law_uid 的法规） */
    private Boolean duplicateFlag;
    private Long duplicateDocumentId;
    private Integer rowNo;
}
