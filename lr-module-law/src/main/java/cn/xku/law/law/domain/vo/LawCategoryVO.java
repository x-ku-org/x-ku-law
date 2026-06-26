package cn.xku.law.law.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LawCategoryVO {
    private Long id;
    private Long parentId;
    private String categoryCode;
    private String categoryName;
    private String categoryType;
    private Integer sortOrder;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
