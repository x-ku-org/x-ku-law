package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 法规条款，对应 lr_law_article */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_article")
public class LawArticleDO extends BaseDO {

    private Long documentId;
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
}
