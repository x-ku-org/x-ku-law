package cn.xku.law.workspace.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FavoriteQueryDTO extends PageParam {
    private String refType;
    private String folderName;
    /** 按收藏对象精确过滤（用于判断某条是否已收藏，避免分页漏判）。 */
    private Long refId;
}
