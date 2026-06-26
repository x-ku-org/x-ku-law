package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 法规版本，对应 lr_law_version */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_version")
public class LawVersionDO extends BaseDO {

    private Long documentId;
    private String versionNo;
    private String versionName;
    /** initial/revised/amended/repealed */
    private String revisionType;
    /** draft/auditing/published/offline */
    private String versionStatus;
    private LocalDate publishDate;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String decisionDocNo;
    private String sourceUrl;
    private Long fileId;
    private String contentText;
    private String contentHash;
    private String diffSummary;
    private String auditStatus;
}
