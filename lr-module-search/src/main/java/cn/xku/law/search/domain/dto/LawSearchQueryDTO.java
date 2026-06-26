package cn.xku.law.search.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LawSearchQueryDTO extends PageParam {
    private String keyword;
    private String effectLevel;
    private String status;
    private String publishAuthority;
    /** 适用地区行政区划代码（精确过滤） */
    private String regionCode;
    /** 排序方式：relevance（相关度，默认）/ time_desc（生效日期降序）/ time_asc（生效日期升序） */
    private String sort;
}
