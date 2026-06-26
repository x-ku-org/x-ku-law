package cn.xku.law.workspace.mapper;

import cn.xku.law.workspace.domain.FavoriteDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** lr_favorite 数据访问层 */
@Mapper
public interface FavoriteMapper extends BaseMapper<FavoriteDO> {
}
