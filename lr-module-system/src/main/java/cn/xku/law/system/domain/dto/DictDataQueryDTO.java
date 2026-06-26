package cn.xku.law.system.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DictDataQueryDTO extends PageParam {
    private String dictCode;
    private String keyword;
    private String status;
}
