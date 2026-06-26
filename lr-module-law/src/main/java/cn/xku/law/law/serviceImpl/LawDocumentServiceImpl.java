package cn.xku.law.law.serviceImpl;

import cn.xku.law.common.constant.EffectLevelMapping;
import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.common.security.SecurityUtils;
import cn.xku.law.law.convert.LawDocumentConvert;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.LawDocumentTagDO;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.domain.TagDO;
import cn.xku.law.law.domain.dto.LawDocumentCreateDTO;
import cn.xku.law.law.domain.dto.LawDocumentQueryDTO;
import cn.xku.law.law.domain.vo.LawDocumentVO;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.law.mapper.LawDocumentTagMapper;
import cn.xku.law.law.mapper.LawVersionMapper;
import cn.xku.law.law.mapper.TagMapper;
import cn.xku.law.law.service.LawDocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/** 法规文件业务实现 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LawDocumentServiceImpl extends ServiceImpl<LawDocumentMapper, LawDocumentDO>
        implements LawDocumentService {

    private final LawDocumentConvert convert;
    private final LawVersionMapper lawVersionMapper;
    private final LawDocumentTagMapper lawDocumentTagMapper;
    private final TagMapper tagMapper;

    @Override
    public PageResult<LawDocumentVO> pageDocuments(LawDocumentQueryDTO query) {
        LambdaQueryWrapper<LawDocumentDO> wrapper = new LambdaQueryWrapper<LawDocumentDO>()
                .in(LawDocumentDO::getTenantId, allowedTenantIds())
                .like(StringUtils.hasText(query.getKeyword()),
                        LawDocumentDO::getTitle, query.getKeyword())
                .eq(StringUtils.hasText(query.getLawType()),
                        LawDocumentDO::getLawType, query.getLawType())
                // 前端传 code，legal_level 列存中文原值；展开为中文原值集合做 in 匹配
                .in(StringUtils.hasText(query.getLegalLevel()),
                        LawDocumentDO::getLegalLevel, EffectLevelMapping.toRawValues(query.getLegalLevel()))
                .eq(StringUtils.hasText(query.getStatus()),
                        LawDocumentDO::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getRegionCode()),
                        LawDocumentDO::getRegionCode, query.getRegionCode())
                .like(StringUtils.hasText(query.getIssuingOrg()),
                        LawDocumentDO::getIssuingOrg, query.getIssuingOrg())
                .orderByDesc(LawDocumentDO::getCreateTime);

        IPage<LawDocumentDO> page = this.page(query.toPage(), wrapper);
        return PageResult.of(page.getTotal(), convert.toVOList(page.getRecords()));
    }

    @Override
    public LawDocumentVO getDocumentById(Long id) {
        LawDocumentDO doc = this.lambdaQuery()
                .eq(LawDocumentDO::getId, id)
                .in(LawDocumentDO::getTenantId, allowedTenantIds())
                .one();
        if (doc == null) {
            throw new AppException(ErrorCode.LAW_DOCUMENT_NOT_FOUND);
        }
        LawDocumentVO vo = convert.toVO(doc);
        vo.setTags(findTagNames(id));
        return vo;
    }

    /** 回挂文档关联的标签名（lr_law_document_tag → lr_tag）。无标签返回空列表。 */
    private List<String> findTagNames(Long documentId) {
        List<Long> tagIds = lawDocumentTagMapper.selectList(
                        new LambdaQueryWrapper<LawDocumentTagDO>()
                                .eq(LawDocumentTagDO::getDocumentId, documentId))
                .stream().map(LawDocumentTagDO::getTagId).toList();
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectList(new LambdaQueryWrapper<TagDO>()
                        .in(TagDO::getId, tagIds))
                .stream().map(TagDO::getTagName).toList();
    }

    @Override
    public Long createDocument(LawDocumentCreateDTO dto) {
        if (lambdaQuery().eq(LawDocumentDO::getLawUid, dto.getLawUid()).exists()) {
            throw new AppException(ErrorCode.LAW_UID_DUPLICATE);
        }
        LawDocumentDO doc = convert.toDO(dto);
        doc.setTenantId(0L);
        this.save(doc);
        return doc.getId();
    }

    @Override
    public void updateDocument(Long id, LawDocumentCreateDTO dto) {
        LawDocumentDO doc = this.getById(id);
        if (doc == null) {
            throw new AppException(ErrorCode.LAW_DOCUMENT_NOT_FOUND);
        }
        convert.updateDO(dto, doc);
        this.updateById(doc);
    }

    @Override
    public void removeDocument(Long id) {
        if (!this.removeById(id)) {
            throw new AppException(ErrorCode.LAW_DOCUMENT_NOT_FOUND);
        }
    }

    @Override
    public void recomputeCurrentVersion(Long documentId) {
        if (documentId == null) {
            return;
        }
        LawDocumentDO doc = this.getById(documentId);
        if (doc == null) {
            return;
        }
        LawVersionDO latest = lawVersionMapper.selectList(
                new LambdaQueryWrapper<LawVersionDO>()
                        .eq(LawVersionDO::getDocumentId, documentId)
                        .eq(LawVersionDO::getVersionStatus, "published")
                        .orderByDesc(LawVersionDO::getPublishDate)
                        .orderByDesc(LawVersionDO::getId)
                        .last("LIMIT 1"))
                .stream().findFirst().orElse(null);
        if (latest == null) {
            return; // 尚无已发布版本，保持原状
        }
        doc.setCurrentVersionId(latest.getId());
        doc.setPublishDate(latest.getPublishDate());
        doc.setEffectiveDate(latest.getEffectiveDate());
        doc.setTimelinessStatus("current");
        this.updateById(doc);
    }

    /** 公共法规（tenant_id=0）+ 当前租户私有法规（未来扩展用） */
    private List<Long> allowedTenantIds() {
        Long currentTenantId = SecurityUtils.getCurrentTenantId();
        if (currentTenantId == null || currentTenantId == 0L) {
            return List.of(0L);
        }
        return Arrays.asList(0L, currentTenantId);
    }
}
