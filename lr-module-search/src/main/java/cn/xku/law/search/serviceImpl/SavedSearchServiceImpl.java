package cn.xku.law.search.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.OwnerValidator;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.search.convert.SavedSearchConvert;
import cn.xku.law.search.domain.SavedSearchDO;
import cn.xku.law.search.domain.dto.SavedSearchCreateDTO;
import cn.xku.law.search.domain.dto.SavedSearchQueryDTO;
import cn.xku.law.search.domain.vo.SavedSearchVO;
import cn.xku.law.search.mapper.SavedSearchMapper;
import cn.xku.law.search.service.SavedSearchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SavedSearchServiceImpl extends ServiceImpl<SavedSearchMapper, SavedSearchDO>
        implements SavedSearchService {

    private final SavedSearchConvert convert;

    @Override
    public PageResult<SavedSearchVO> pageSavedSearches(SavedSearchQueryDTO query) {
        Long userId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<SavedSearchDO> wrapper = new LambdaQueryWrapper<SavedSearchDO>()
                .eq(SavedSearchDO::getUserId, userId)
                .eq(StringUtils.hasText(query.getStatus()), SavedSearchDO::getStatus, query.getStatus())
                .orderByDesc(SavedSearchDO::getCreateTime);
        IPage<SavedSearchDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public Long createSavedSearch(SavedSearchCreateDTO dto) {
        SavedSearchDO entity = convert.toDO(dto);
        entity.setUserId(SecurityUtils.getCurrentUserId());
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void updateSavedSearch(Long id, SavedSearchCreateDTO dto) {
        SavedSearchDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.SAVED_SEARCH_NOT_FOUND);
        OwnerValidator.checkOwner(entity.getUserId());
        convert.updateDO(dto, entity);
        this.updateById(entity);
    }

    @Override
    public void removeSavedSearch(Long id) {
        SavedSearchDO entity = this.getById(id);
        if (entity == null) throw new AppException(ErrorCode.SAVED_SEARCH_NOT_FOUND);
        OwnerValidator.checkOwner(entity.getUserId());
        this.removeById(id);
    }
}
