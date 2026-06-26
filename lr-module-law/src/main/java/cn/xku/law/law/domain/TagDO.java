package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 标签，对应 lr_tag */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_tag")
public class TagDO extends BaseDO {

    /** 标签编码，唯一键 uk_tag_code(tenant_id, tag_code) 的业务键 */
    private String tagCode;
    private String tagName;
    /** 标签类型：law/topic/user/system */
    private String tagType;
    private String status;
}
