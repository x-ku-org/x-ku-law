package cn.xku.law.law.serviceImpl;

import cn.xku.law.common.exception.AppException;
import cn.xku.law.common.exception.ErrorCode;
import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.convert.LawVersionConvert;
import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.domain.SearchIndexTaskDO;
import cn.xku.law.law.domain.VectorSyncTaskDO;
import cn.xku.law.law.domain.dto.LawVersionCreateDTO;
import cn.xku.law.law.domain.dto.LawVersionQueryDTO;
import cn.xku.law.law.domain.vo.LawVersionVO;
import cn.xku.law.law.event.LawVersionPublishedEvent;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.law.mapper.LawVersionMapper;
import cn.xku.law.law.mapper.SearchIndexTaskMapper;
import cn.xku.law.law.mapper.VectorSyncTaskMapper;
import cn.xku.law.law.service.LawVersionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LawVersionServiceImpl extends ServiceImpl<LawVersionMapper, LawVersionDO>
        implements LawVersionService {

    private final LawVersionConvert convert;
    private final LawDocumentMapper lawDocumentMapper;
    private final SearchIndexTaskMapper searchIndexTaskMapper;
    private final VectorSyncTaskMapper vectorSyncTaskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.vector.index-name:law_segment}")
    private String vectorIndexName;

    @Override
    public PageResult<LawVersionVO> pageVersions(LawVersionQueryDTO query) {
        LambdaQueryWrapper<LawVersionDO> wrapper = new LambdaQueryWrapper<LawVersionDO>()
                .eq(query.getDocumentId() != null, LawVersionDO::getDocumentId, query.getDocumentId())
                .eq(StringUtils.hasText(query.getVersionStatus()),
                        LawVersionDO::getVersionStatus, query.getVersionStatus())
                .eq(StringUtils.hasText(query.getAuditStatus()),
                        LawVersionDO::getAuditStatus, query.getAuditStatus())
                .eq(StringUtils.hasText(query.getRevisionType()),
                        LawVersionDO::getRevisionType, query.getRevisionType())
                .orderByDesc(LawVersionDO::getCreateTime);
        IPage<LawVersionDO> page = this.page(query.toPage(), wrapper);
        List<LawVersionVO> list = convert.toVOList(page.getRecords());
        Map<Long, String> titles = LawTitleHelper.titlesByIds(lawDocumentMapper,
                list.stream().map(LawVersionVO::getDocumentId).collect(Collectors.toList()));
        list.forEach(v -> v.setDocumentTitle(titles.get(v.getDocumentId())));
        return PageResult.of(page.getTotal(), list);
    }

    @Override
    public LawVersionVO getVersionById(Long id) {
        LawVersionDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_VERSION_NOT_FOUND);
        }
        return convert.toVO(entity);
    }

    @Override
    public Long createVersion(LawVersionCreateDTO dto) {
        LawVersionDO entity = convert.toDO(dto);
        this.save(entity);
        return entity.getId();
    }

    @Override
    public void updateVersion(Long id, LawVersionCreateDTO dto) {
        LawVersionDO entity = this.getById(id);
        if (entity == null) {
            throw new AppException(ErrorCode.LAW_VERSION_NOT_FOUND);
        }
        convert.updateDO(dto, entity);
        this.updateById(entity);
    }

    @Override
    public void removeVersion(Long id) {
        if (!this.removeById(id)) {
            throw new AppException(ErrorCode.LAW_VERSION_NOT_FOUND);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(Long id) {
        // 注意：自调用会绕过代理，被调方法的 @Transactional 不生效，故本入口也需独立标注事务。
        publishVersion(id, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishVersion(Long id, boolean suppressSubscriptionAlert) {
        LawVersionDO version = this.getById(id);
        if (version == null) throw new AppException(ErrorCode.LAW_VERSION_NOT_FOUND);
        if ("published".equals(version.getVersionStatus())) {
            throw new AppException(ErrorCode.LAW_VERSION_ALREADY_PUBLISHED);
        }

        version.setVersionStatus("published");
        this.updateById(version);

        LawDocumentDO doc = lawDocumentMapper.selectById(version.getDocumentId());
        if (doc != null) {
            doc.setCurrentVersionId(id);
            lawDocumentMapper.updateById(doc);
        }

        enqueueSearchIndex(id);

        VectorSyncTaskDO vectorTask = new VectorSyncTaskDO();
        vectorTask.setRefType("law_version");
        vectorTask.setRefId(id);
        vectorTask.setActionType("upsert");
        vectorTask.setSyncStatus("pending");
        vectorTask.setVectorIndex(vectorIndexName);
        vectorTask.setRetryCount(0);
        vectorSyncTaskMapper.insert(vectorTask);

        String matchType = mapRevisionTypeToMatchType(version.getRevisionType());
        eventPublisher.publishEvent(
                new LawVersionPublishedEvent(this, version.getDocumentId(), id, matchType,
                        suppressSubscriptionAlert));
    }

    @Override
    public void enqueueSearchIndex(Long versionId) {
        SearchIndexTaskDO indexTask = new SearchIndexTaskDO();
        indexTask.setRefType("law_version");
        indexTask.setRefId(versionId);
        indexTask.setIndexName("law_document");
        indexTask.setActionType("upsert");
        indexTask.setSyncStatus("pending");
        indexTask.setRetryCount(0);
        searchIndexTaskMapper.insert(indexTask);
    }

    private String mapRevisionTypeToMatchType(String revisionType) {
        if ("repealed".equals(revisionType)) return "repeal";
        if ("initial".equals(revisionType)) return "new";
        return "update"; // revised / amended
    }
}
