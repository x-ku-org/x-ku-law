package cn.xku.law.law.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.LawCategoryDO;
import cn.xku.law.law.domain.dto.LawCategoryCreateDTO;
import cn.xku.law.law.domain.dto.LawCategoryQueryDTO;
import cn.xku.law.law.domain.vo.LawCategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface LawCategoryService extends IService<LawCategoryDO> {

    PageResult<LawCategoryVO> pageCategories(LawCategoryQueryDTO query);

    List<LawCategoryVO> listAll();

    LawCategoryVO getCategoryById(Long id);

    Long createCategory(LawCategoryCreateDTO dto);

    void updateCategory(Long id, LawCategoryCreateDTO dto);

    void removeCategory(Long id);
}
