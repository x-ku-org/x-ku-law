package cn.xku.law.system.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.convert.DictTypeConvert;
import cn.xku.law.system.domain.DictTypeDO;
import cn.xku.law.system.domain.dto.DictTypeCreateDTO;
import cn.xku.law.system.domain.dto.DictTypeQueryDTO;
import cn.xku.law.system.domain.vo.DictTypeVO;
import cn.xku.law.system.mapper.DictTypeMapper;
import cn.xku.law.system.service.DictTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictTypeDO>
        implements DictTypeService {

    private final DictTypeConvert convert;

    @Override
    public PageResult<DictTypeVO> pageDictTypes(DictTypeQueryDTO query) {
        LambdaQueryWrapper<DictTypeDO> wrapper = new LambdaQueryWrapper<DictTypeDO>()
                .like(StringUtils.hasText(query.getKeyword()), DictTypeDO::getDictName, query.getKeyword())
                .eq(StringUtils.hasText(query.getStatus()), DictTypeDO::getStatus, query.getStatus())
                .orderByDesc(DictTypeDO::getCreateTime);
        IPage<DictTypeDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public Long createDictType(DictTypeCreateDTO dto) {
        DictTypeDO dictType = convert.toDO(dto);
        this.save(dictType);
        return dictType.getId();
    }

    @Override
    public void updateDictType(Long id, DictTypeCreateDTO dto) {
        DictTypeDO dictType = this.getById(id);
        if (dictType == null) throw new AppException(ErrorCode.SYS_DICT_NOT_FOUND);
        convert.updateDO(dto, dictType);
        this.updateById(dictType);
    }

    @Override
    public void removeDictType(Long id) {
        if (!this.removeById(id)) throw new AppException(ErrorCode.SYS_DICT_NOT_FOUND);
    }
}
