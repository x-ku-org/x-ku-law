package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 法规-分类关联（中间表），对应 lr_law_document_category */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_document_category")
public class LawDocumentCategoryDO extends BaseDO {

    private Long documentId;
    private Long categoryId;
}
