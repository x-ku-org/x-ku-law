package cn.xku.law.support.mapper;

import cn.xku.law.support.domain.TicketDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** lr_ticket 数据访问层 */
@Mapper
public interface TicketMapper extends BaseMapper<TicketDO> {
}
