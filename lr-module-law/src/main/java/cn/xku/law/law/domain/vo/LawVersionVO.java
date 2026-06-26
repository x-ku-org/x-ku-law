package cn.xku.law.law.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LawVersionVO {
    private Long id;
    private Long documentId;
    /** 冗余：所属法规标题，便于前端列表/选择器直接展示可读名（分页查询时回填）。 */
    private String documentTitle;
    private String versionNo;
    private String versionName;
    private String revisionType;
    private String versionStatus;
    private LocalDate publishDate;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String decisionDocNo;
    private String sourceUrl;
    private Long fileId;
    private String contentHash;
    private String diffSummary;
    private String auditStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
