package cn.xku.law.law.serviceImpl;

import cn.xku.law.law.diff.ArticleDiffService;
import cn.xku.law.law.diff.VersionDiffResult;
import cn.xku.law.law.domain.CompareRecordDO;
import cn.xku.law.law.mapper.CompareRecordMapper;
import cn.xku.law.law.service.CompareRecordService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompareRecordServiceImpl extends ServiceImpl<CompareRecordMapper, CompareRecordDO>
        implements CompareRecordService {

    private final ObjectMapper objectMapper;
    private final ArticleDiffService articleDiffService;

    @Override
    public Long saveSystemDiff(Long documentId, VersionDiffResult diff) {
        // 幂等：清掉同一对版本的旧系统记录（表无唯一键，靠条件删除保证单条）。
        baseMapper.deleteSystemDiff(SYSTEM_USER_ID, diff.getBaseVersionId(), diff.getTargetVersionId());

        CompareRecordDO record = new CompareRecordDO();
        record.setUserId(SYSTEM_USER_ID);
        record.setDocumentId(documentId);
        record.setBaseVersionId(diff.getBaseVersionId());
        record.setTargetVersionId(diff.getTargetVersionId());
        record.setCompareType("article");
        record.setResultJson(toJson(diff));
        baseMapper.insertSystemDiff(record);
        return record.getId();
    }

    @Override
    public CompareRecordDO findSystemDiff(Long baseVersionId, Long targetVersionId) {
        return baseMapper.selectSystemDiff(SYSTEM_USER_ID, baseVersionId, targetVersionId);
    }

    @Override
    public VersionDiffResult getOrComputeDiff(Long documentId, Long baseVersionId, Long targetVersionId) {
        CompareRecordDO cached = findSystemDiff(baseVersionId, targetVersionId);
        if (cached != null && StringUtils.hasText(cached.getResultJson())) {
            VersionDiffResult parsed = fromJson(cached.getResultJson());
            if (parsed != null) {
                return parsed;
            }
        }
        VersionDiffResult diff = articleDiffService.diff(baseVersionId, targetVersionId);
        saveSystemDiff(documentId, diff);
        return diff;
    }

    private String toJson(VersionDiffResult diff) {
        try {
            return objectMapper.writeValueAsString(diff);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化版本对比结果失败", e);
        }
    }

    private VersionDiffResult fromJson(String json) {
        try {
            return objectMapper.readValue(json, VersionDiffResult.class);
        } catch (JsonProcessingException e) {
            log.warn("[CompareRecord] 反序列化对比结果失败，将重算: {}", e.getMessage());
            return null;
        }
    }
}
