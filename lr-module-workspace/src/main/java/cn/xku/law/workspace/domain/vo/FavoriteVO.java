package cn.xku.law.workspace.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteVO {
    private Long id;
    private Long userId;
    private String refType;
    private Long refId;
    private String folderName;
    private String titleSnapshot;
    private LocalDateTime createTime;
}
