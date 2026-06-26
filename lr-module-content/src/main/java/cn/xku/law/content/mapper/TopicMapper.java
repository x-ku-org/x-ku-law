package cn.xku.law.content.mapper;

import cn.xku.law.content.domain.TopicDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** lr_topic 数据访问层 */
@Mapper
public interface TopicMapper extends BaseMapper<TopicDO> {
}
