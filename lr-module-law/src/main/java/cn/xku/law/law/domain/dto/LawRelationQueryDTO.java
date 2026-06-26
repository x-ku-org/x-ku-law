package cn.xku.law.law.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LawRelationQueryDTO extends PageParam {
    private Long sourceDocumentId;
    private Long targetDocumentId;
    private String relationType;
}
