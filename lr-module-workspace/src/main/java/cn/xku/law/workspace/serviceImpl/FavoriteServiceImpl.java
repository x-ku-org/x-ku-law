package cn.xku.law.workspace.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.workspace.convert.FavoriteConvert;
import cn.xku.law.workspace.domain.FavoriteDO;
import cn.xku.law.workspace.domain.dto.FavoriteCreateDTO;
import cn.xku.law.workspace.domain.dto.FavoriteQueryDTO;
import cn.xku.law.workspace.domain.vo.FavoriteVO;
import cn.xku.law.workspace.mapper.FavoriteMapper;
import cn.xku.law.workspace.service.FavoriteService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, FavoriteDO>
        implements FavoriteService {

    private final FavoriteConvert convert;

    @Override
    public PageResult<FavoriteVO> pageFavorites(FavoriteQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<FavoriteDO> wrapper = new LambdaQueryWrapper<FavoriteDO>()
                .eq(FavoriteDO::getUserId, userId)
                .eq(StringUtils.hasText(query.getRefType()), FavoriteDO::getRefType, query.getRefType())
                .eq(query.getRefId() != null, FavoriteDO::getRefId, query.getRefId())
                .eq(StringUtils.hasText(query.getFolderName()), FavoriteDO::getFolderName, query.getFolderName())
                .orderByDesc(FavoriteDO::getCreateTime);
        IPage<FavoriteDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public Long addFavorite(FavoriteCreateDTO dto) {
        FavoriteDO entity = convert.toDO(dto);
        entity.setUserId(SecurityUtils.getCurrentUserId());
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void removeFavorite(Long id) {
        FavoriteDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.NOT_FOUND);
        Long userId = SecurityUtils.getCurrentUserId();
        if (!entity.getUserId().equals(userId)) throw new AppException(ErrorCode.FORBIDDEN);
        this.removeById(id);
    }
}
