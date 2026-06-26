package cn.xku.law.system.mapper;

import cn.xku.law.system.domain.TenantDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** lr_tenant 数据访问层。其余 system 表 Mapper 结构相同，TODO: 按需添加 */
@Mapper
public interface TenantMapper extends BaseMapper<TenantDO> {
}
