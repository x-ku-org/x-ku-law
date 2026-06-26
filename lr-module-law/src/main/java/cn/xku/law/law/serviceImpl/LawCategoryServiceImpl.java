package cn.xku.law.law.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.convert.LawCategoryConvert;
import cn.xku.law.law.domain.LawCategoryDO;
import cn.xku.law.law.domain.dto.LawCategoryCreateDTO;
import cn.xku.law.law.domain.dto.LawCategoryQueryDTO;
import cn.xku.law.law.domain.vo.LawCategoryVO;
import cn.xku.law.law.mapper.LawCategoryMapper;
import cn.xku.law.law.service.LawCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LawCategoryServiceImpl extends ServiceImpl<LawCategoryMapper, LawCategoryDO>
        implements LawCategoryService {

    private final LawCategoryConvert convert;

    @Override
    public PageResult<LawCategoryVO> pageCategories(LawCategoryQueryDTO query) {
        LambdaQueryWrapper<LawCategoryDO> wrapper = new LambdaQueryWrapper<LawCategoryDO>()
                .eq(query.getParentId() != null, LawCategoryDO::getParentId, query.getParentId())
                .eq(StringUtils.hasText(query.getCategoryType()),
                        LawCategoryDO::getCategoryType, query.getCategoryType())
                .like(StringUtils.hasText(query.getKeyword()),
                        LawCategoryDO::getCategoryName, query.getKeyword())
                .eq(StringUtils.hasText(query.getStatus()), LawCategoryDO::getStatus, query.getStatus())
                .orderByAsc(LawCategoryDO::getSortOrder);
        IPage<LawCategoryDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public List<LawCategoryVO> listAll() {
        List<LawCategoryDO> list = this.list(new LambdaQueryWrapper<LawCategoryDO>()
                .eq(LawCategoryDO::getStatus, "enabled")
                .orderByAsc(LawCategoryDO::getSortOrder));
        return convert.toVOList(list);
    }

    @Override
    public LawCategoryVO getCategoryById(Long id) {
        LawCategoryDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_CATEGORY_NOT_FOUND);
        }
        return convert.toVO(entity);
    }

    @Override
    public Long createCategory(LawCategoryCreateDTO dto) {
        LawCategoryDO entity = convert.toDO(dto);
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void updateCategory(Long id, LawCategoryCreateDTO dto) {
        LawCategoryDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_CATEGORY_NOT_FOUND);
        }
        convert.updateDO(dto, entity);
        this.updateById(entity);
    }

    @Override
    public void removeCategory(Long id) {
        if (!this.removeById(id)) {
            throw new AppException(ErrorCode.LAW_CATEGORY_NOT_FOUND);
        }
    }
}
