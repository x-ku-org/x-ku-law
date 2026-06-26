package cn.xku.law.search.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SavedSearchVO {
    private Long id;
    private Long userId;
    private String name;
    private String keyword;
    private String filtersJson;
    private Boolean notifyEnabled;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
