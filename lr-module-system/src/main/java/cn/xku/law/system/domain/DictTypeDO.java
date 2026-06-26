package cn.xku.law.system.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 字典类型表，对应 lr_dict_type */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_dict_type")
public class DictTypeDO extends BaseDO {

    private String dictCode;
    private String dictName;
    /** enabled/disabled */
    private String status;
    private String remark;
}
