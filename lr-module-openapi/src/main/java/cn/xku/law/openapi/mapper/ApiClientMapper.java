package cn.xku.law.openapi.mapper;

import cn.xku.law.openapi.domain.ApiClientDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** lr_api_client 数据访问层 */
@Mapper
public interface ApiClientMapper extends BaseMapper<ApiClientDO> {
}
