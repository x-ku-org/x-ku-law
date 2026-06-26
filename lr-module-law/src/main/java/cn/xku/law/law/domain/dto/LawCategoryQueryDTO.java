package cn.xku.law.law.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LawCategoryQueryDTO extends PageParam {
    private Long parentId;
    private String categoryType;
    private String keyword;
    private String status;
}
