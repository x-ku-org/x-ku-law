package cn.xku.law.collect.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容采集来源，对应 lr_content_source。
 * 此包下其余 DO（CollectTaskDO / CollectRecordDO / ImportBatchDO / ImportRecordDO /
 * RawDocumentDO / DataQualityIssueDO / DataAuditRecordDO）结构相同，TODO: 按需补全。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_content_source")
public class ContentSourceDO extends BaseDO {

    private String sourceName;
    private String sourceType;
    private String sourceUrl;
    private String status;
}
