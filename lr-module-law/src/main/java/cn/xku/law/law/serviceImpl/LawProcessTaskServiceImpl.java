package cn.xku.law.law.serviceImpl;

import cn.xku.law.law.domain.LawProcessTaskDO;
import cn.xku.law.law.mapper.LawProcessTaskMapper;
import cn.xku.law.law.service.LawProcessTaskService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LawProcessTaskServiceImpl extends ServiceImpl<LawProcessTaskMapper, LawProcessTaskDO>
        implements LawProcessTaskService {

    @Override
    public Long enqueue(Long documentId, Long versionId, Long fileId) {
        if (versionId == null) {
            return null;
        }
        if (baseMapper.countActiveByVersion(versionId) > 0) {
            log.debug("[LawProcessTask] versionId={} already has an active task, skip enqueue", versionId);
            return null;
        }
        LawProcessTaskDO task = new LawProcessTaskDO();
        task.setDocumentId(documentId);
        task.setVersionId(versionId);
        task.setFileId(fileId);
        task.setProcessStatus("pending");
        task.setRetryCount(0);
        try {
            this.save(task);
        } catch (DuplicateKeyException e) {
            // countActiveByVersion 与 save 非原子，并发入队可能都通过 count 检查；
            // uk_active_version 唯一键兜底拦下重复，命中即说明同 version 已有在途任务，按已入队处理。
            log.debug("[LawProcessTask] versionId={} concurrent enqueue blocked by uk_active_version, skip", versionId);
            return null;
        }
        return task.getId();
    }
}
