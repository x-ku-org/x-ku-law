package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 法规解读结果，对应 lr_law_interpretation。整篇文档级解读，每个已发布版本一条。
 * 由 InterpretationStage（AI 旁路）生成；公共法规数据，tenant_id=0，全租户可读。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_interpretation")
public class LawInterpretationDO extends BaseDO {

    private Long documentId;
    private Long versionId;
    /** 生成所用 AI 模型标识 */
    private String model;
    /** 整篇解读正文 */
    private String interpretationText;
    /** done/failed */
    private String status;
    private Integer tokenCount;
}
