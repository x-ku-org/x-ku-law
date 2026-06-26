package cn.xku.law.search.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SavedSearchQueryDTO extends PageParam {
    private String status;
}
