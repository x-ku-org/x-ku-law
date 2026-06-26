package cn.xku.law.law.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LawArticleQueryDTO extends PageParam {
    private Long documentId;
    private Long versionId;
    private Long parentArticleId;
    private String keyword;
    private String status;
    private Boolean obligationFlag;
    private Boolean penaltyFlag;
}
