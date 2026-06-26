package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 法规分类，对应 lr_law_category */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_category")
public class LawCategoryDO extends BaseDO {

    private Long parentId;
    private String categoryCode;
    private String categoryName;
    /** subject/region/industry/legal_level */
    private String categoryType;
    private Integer sortOrder;
    private String status;
}
