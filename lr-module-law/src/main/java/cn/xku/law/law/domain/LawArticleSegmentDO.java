package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 条款分片（向量化单元），对应 lr_law_article_segment。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_article_segment")
public class LawArticleSegmentDO extends BaseDO {

    private Long articleId;
    private Long versionId;
    /** 同一条款内的分片序号，从 1 开始（uk: tenant_id, article_id, segment_no） */
    private Integer segmentNo;
    private String segmentText;
    private String segmentHash;
    /** token 数（粗略估算） */
    private Integer tokenCount;
    /** 向量化状态：pending/processing/done/failed */
    private String embeddingStatus;
    /** 向量入 ES 后的向量 ID */
    private String vectorId;
}
