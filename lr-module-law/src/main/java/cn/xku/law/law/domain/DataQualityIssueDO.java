package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/** 数据质量问题，对应 lr_data_quality_issue（平台表 tenant_id=0） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_data_quality_issue")
public class DataQualityIssueDO extends BaseDO {

    /** law_document/law_version/law_article/raw_document */
    private String refType;
    private Long refId;
    /** duplicate/missing_field/parse_error/status_conflict/citation_error */
    private String issueType;
    /** low/normal/high/critical */
    private String issueLevel;
    private String issueDesc;
    /** open/acknowledged/resolved/closed */
    private String status;
    private Long ownerUserId;
    private LocalDateTime resolvedTime;
}
