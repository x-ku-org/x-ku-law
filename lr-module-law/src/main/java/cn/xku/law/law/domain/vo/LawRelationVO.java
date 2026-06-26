package cn.xku.law.law.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LawRelationVO {
    private Long id;
    private Long sourceDocumentId;
    private Long sourceVersionId;
    private Long targetDocumentId;
    private Long targetVersionId;
    private Long sourceArticleId;
    private Long targetArticleId;
    private String relationType;
    private String relationDesc;
    private LocalDate relationDate;
    /** 冗余：来源/目标法规标题，便于前端列表/选择器直接展示可读名（分页查询时回填）。 */
    private String sourceTitle;
    private String targetTitle;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
