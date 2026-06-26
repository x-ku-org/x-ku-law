package cn.xku.law.workspace.convert;

import cn.xku.law.workspace.domain.FavoriteDO;
import cn.xku.law.workspace.domain.dto.FavoriteCreateDTO;
import cn.xku.law.workspace.domain.vo.FavoriteVO;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FavoriteConvert {

    FavoriteVO toVO(FavoriteDO entity);

    List<FavoriteVO> toVOList(List<FavoriteDO> list);

    FavoriteDO toDO(FavoriteCreateDTO dto);
}
