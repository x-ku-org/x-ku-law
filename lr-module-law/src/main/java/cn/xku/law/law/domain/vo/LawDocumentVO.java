package cn.xku.law.law.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 法规文件列表/详情响应视图，按需字段裁剪 */
@Data
public class LawDocumentVO {

    private Long id;
    private String lawUid;
    private String title;
    private String documentNo;
    private String lawType;
    private String legalLevel;
    private String issuingOrg;
    private String regionCode;
    private String industryCode;
    private String subjectDomain;
    private String status;
    private LocalDate publishDate;
    private LocalDate effectiveDate;
    private LocalDate expireDate;
    private String timelinessStatus;
    private Long currentVersionId;
    private String summary;
    private String officialUrl;
    /** 关联标签名（详情接口回挂；列表不填） */
    private List<String> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
