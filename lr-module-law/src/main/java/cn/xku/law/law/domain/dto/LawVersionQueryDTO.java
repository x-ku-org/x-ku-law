package cn.xku.law.law.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LawVersionQueryDTO extends PageParam {
    private Long documentId;
    private String versionStatus;
    private String auditStatus;
    private String revisionType;
}
