package cn.xku.law.law.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.convert.LawRelationConvert;
import cn.xku.law.law.domain.LawRelationDO;
import cn.xku.law.law.domain.dto.LawRelationCreateDTO;
import cn.xku.law.law.domain.dto.LawRelationQueryDTO;
import cn.xku.law.law.domain.vo.LawRelationVO;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.law.mapper.LawRelationMapper;
import cn.xku.law.law.service.LawRelationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LawRelationServiceImpl extends ServiceImpl<LawRelationMapper, LawRelationDO>
        implements LawRelationService {

    private final LawRelationConvert convert;
    private final LawDocumentMapper lawDocumentMapper;

    @Override
    public PageResult<LawRelationVO> pageRelations(LawRelationQueryDTO query) {
        LambdaQueryWrapper<LawRelationDO> wrapper = new LambdaQueryWrapper<LawRelationDO>()
                .eq(query.getSourceDocumentId() != null,
                        LawRelationDO::getSourceDocumentId, query.getSourceDocumentId())
                .eq(query.getTargetDocumentId() != null,
                        LawRelationDO::getTargetDocumentId, query.getTargetDocumentId())
                .eq(StringUtils.hasText(query.getRelationType()),
                        LawRelationDO::getRelationType, query.getRelationType())
                .orderByDesc(LawRelationDO::getCreateTime);
        IPage<LawRelationDO> page = this.page(query.toPage(), wrapper);
        List<LawRelationVO> list = convert.toVOList(page.getRecords());
        Map<Long, String> titles = LawTitleHelper.titlesByIds(lawDocumentMapper,
                list.stream()
                        .flatMap(v -> Stream.of(v.getSourceDocumentId(), v.getTargetDocumentId()))
                        .collect(Collectors.toList()));
        list.forEach(v -> {
            v.setSourceTitle(titles.get(v.getSourceDocumentId()));
            v.setTargetTitle(titles.get(v.getTargetDocumentId()));
        });
        return PageResult.of(page.getTotal(), list);
    }

    @Override
    public LawRelationVO getRelationById(Long id) {
        LawRelationDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_RELATION_NOT_FOUND);
        }
        return convert.toVO(entity);
    }

    @Override
    public Long createRelation(LawRelationCreateDTO dto) {
        LawRelationDO entity = convert.toDO(dto);
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void updateRelation(Long id, LawRelationCreateDTO dto) {
        LawRelationDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_RELATION_NOT_FOUND);
        }
        convert.updateDO(dto, entity);
        this.updateById(entity);
    }

    @Override
    public void removeRelation(Long id) {
        if (!this.removeById(id)) {
            throw new AppException(ErrorCode.LAW_RELATION_NOT_FOUND);
        }
    }
}
