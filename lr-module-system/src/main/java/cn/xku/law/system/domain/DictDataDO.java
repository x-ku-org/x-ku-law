package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 字典数据表，对应 lr_dict_data */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_dict_data")
public class DictDataDO extends BaseDO {

    private Long dictTypeId;
    private String dictCode;
    private String dictLabel;
    private String dictValue;
    private String parentValue;
    private Integer sortOrder;
    /** enabled/disabled */
    private String status;
    private String extJson;
}
