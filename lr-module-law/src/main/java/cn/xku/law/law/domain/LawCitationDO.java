package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** 条款引证，对应 lr_law_citation */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_citation")
public class LawCitationDO extends BaseDO {

    private Long fromArticleId;
    private Long toDocumentId;
    private Long toVersionId;
    private Long toArticleId;
    private String citationText;
    /** explicit/implicit/manual */
    private String citationType;
    private BigDecimal confidenceScore;
}
