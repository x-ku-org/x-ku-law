package cn.xku.law.content.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运营专题，对应 lr_topic。
 * 此包下其余 DO（TopicItemDO / BannerDO / HelpArticleDO）结构相同，TODO: 按需补全。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_topic")
public class TopicDO extends BaseDO {

    private String topicTitle;
    private String coverImageUrl;
    private String description;
    private String status;
    private Integer sortOrder;
}
