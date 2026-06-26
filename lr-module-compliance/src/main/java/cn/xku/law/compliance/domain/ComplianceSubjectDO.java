package cn.xku.law.compliance.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 合规主体，对应 lr_compliance_subject。
 * 此包下其余 DO（ComplianceObligationDO / ComplianceListDO / ComplianceListItemDO /
 * ComplianceTaskDO / ComplianceEvidenceDO / RiskAssessmentDO / ComplianceReportDO）
 * 结构相同，TODO: 按需补全。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_compliance_subject")
public class ComplianceSubjectDO extends BaseDO {

    private String subjectName;
    private String subjectType;
    private String industryCode;
    private String regionCode;
    private String status;
    private Long ownerId;
}
