package cn.xku.law.search.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.search.domain.SavedSearchDO;
import cn.xku.law.search.domain.dto.SavedSearchCreateDTO;
import cn.xku.law.search.domain.dto.SavedSearchQueryDTO;
import cn.xku.law.search.domain.vo.SavedSearchVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SavedSearchService extends IService<SavedSearchDO> {

    PageResult<SavedSearchVO> pageSavedSearches(SavedSearchQueryDTO query);

    Long createSavedSearch(SavedSearchCreateDTO dto);

    void updateSavedSearch(Long id, SavedSearchCreateDTO dto);

    void removeSavedSearch(Long id);
}
