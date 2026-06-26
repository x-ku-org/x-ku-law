package cn.xku.law.system.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.system.convert.DictDataConvert;
import cn.xku.law.system.domain.DictDataDO;
import cn.xku.law.system.domain.dto.DictDataCreateDTO;
import cn.xku.law.system.domain.dto.DictDataQueryDTO;
import cn.xku.law.system.domain.vo.DictDataVO;
import cn.xku.law.system.mapper.DictDataMapper;
import cn.xku.law.system.service.DictDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictDataDO>
        implements DictDataService {

    private final DictDataConvert convert;

    @Override
    public PageResult<DictDataVO> pageDictData(DictDataQueryDTO query) {
        LambdaQueryWrapper<DictDataDO> wrapper = new LambdaQueryWrapper<DictDataDO>()
                .eq(StringUtils.hasText(query.getDictCode()), DictDataDO::getDictCode, query.getDictCode())
                .like(StringUtils.hasText(query.getKeyword()), DictDataDO::getDictLabel, query.getKeyword())
                .eq(StringUtils.hasText(query.getStatus()), DictDataDO::getStatus, query.getStatus())
                .orderByAsc(DictDataDO::getSortOrder);
        IPage<DictDataDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public List<DictDataVO> listByDictCode(String dictCode) {
        return convert.toVOList(this.list(new LambdaQueryWrapper<DictDataDO>()
                .eq(DictDataDO::getDictCode, dictCode)
                .eq(DictDataDO::getStatus, "enabled")
                .orderByAsc(DictDataDO::getSortOrder)));
    }

    @Override
    public Map<String, List<DictDataVO>> listByDictCodes(List<String> dictCodes) {
        if (CollectionUtils.isEmpty(dictCodes)) return Map.of();
        List<DictDataVO> all = convert.toVOList(this.list(new LambdaQueryWrapper<DictDataDO>()
                .in(DictDataDO::getDictCode, dictCodes)
                .eq(DictDataDO::getStatus, "enabled")
                .orderByAsc(DictDataDO::getSortOrder)));
        return all.stream().collect(Collectors.groupingBy(DictDataVO::getDictCode));
    }

    @Override
    public Long createDictData(DictDataCreateDTO dto) {
        DictDataDO dictData = convert.toDO(dto);
        this.save(dictData);
        return dictData.getId();
    }

    @Override
    public void updateDictData(Long id, DictDataCreateDTO dto) {
        DictDataDO dictData = this.getById(id);
        if (dictData == null) throw new AppException(ErrorCode.SYS_DICT_NOT_FOUND);
        convert.updateDO(dto, dictData);
        this.updateById(dictData);
    }

    @Override
    public void removeDictData(Long id) {
        if (!this.removeById(id)) throw new AppException(ErrorCode.SYS_DICT_NOT_FOUND);
    }
}
