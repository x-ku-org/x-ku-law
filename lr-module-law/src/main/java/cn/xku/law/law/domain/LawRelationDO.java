package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 法规关系，对应 lr_law_relation */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_relation")
public class LawRelationDO extends BaseDO {

    private Long sourceDocumentId;
    private Long sourceVersionId;
    private Long targetDocumentId;
    private Long targetVersionId;
    private Long sourceArticleId;
    private Long targetArticleId;
    /** amend/repeal/cite/interpret/support/conflict */
    private String relationType;
    private String relationDesc;
    private LocalDate relationDate;
}
