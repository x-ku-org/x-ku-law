package cn.xku.law.workspace.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.workspace.domain.FavoriteDO;
import cn.xku.law.workspace.domain.dto.FavoriteCreateDTO;
import cn.xku.law.workspace.domain.dto.FavoriteQueryDTO;
import cn.xku.law.workspace.domain.vo.FavoriteVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface FavoriteService extends IService<FavoriteDO> {

    PageResult<FavoriteVO> pageFavorites(FavoriteQueryDTO query);

    Long addFavorite(FavoriteCreateDTO dto);

    void removeFavorite(Long id);
}
