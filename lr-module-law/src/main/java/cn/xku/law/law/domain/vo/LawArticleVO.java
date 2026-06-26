package cn.xku.law.law.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LawArticleVO {
    private Long id;
    private Long documentId;
    /** 冗余：所属法规标题，便于前端列表/选择器直接展示可读名（分页查询时回填）。 */
    private String documentTitle;
    private Long versionId;
    private Long parentArticleId;
    private String articleNo;
    private String articleTitle;
    private String chapterNo;
    private String chapterTitle;
    private String sectionNo;
    private String sectionTitle;
    private Integer articleOrder;
    private Integer articleLevel;
    private String contentText;
    private String contentHash;
    private Boolean obligationFlag;
    private Boolean penaltyFlag;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
