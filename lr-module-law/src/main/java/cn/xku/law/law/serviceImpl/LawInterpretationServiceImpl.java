package cn.xku.law.law.serviceImpl;

import cn.xku.law.law.domain.LawInterpretationDO;
import cn.xku.law.law.mapper.LawInterpretationMapper;
import cn.xku.law.law.service.LawInterpretationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LawInterpretationServiceImpl
        extends ServiceImpl<LawInterpretationMapper, LawInterpretationDO>
        implements LawInterpretationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveForVersion(Long documentId, Long versionId, String model, String text) {
        baseMapper.physicalDeleteByVersion(versionId);
        LawInterpretationDO entity = new LawInterpretationDO();
        entity.setDocumentId(documentId);
        entity.setVersionId(versionId);
        entity.setModel(model);
        entity.setInterpretationText(text);
        entity.setStatus("done");
        entity.setTokenCount(text == null ? 0 : text.length());
        this.save(entity);
    }

    @Override
    public LawInterpretationDO getByVersionId(Long versionId) {
        if (versionId == null) {
            return null;
        }
        return this.getOne(new LambdaQueryWrapper<LawInterpretationDO>()
                .eq(LawInterpretationDO::getVersionId, versionId)
                .last("LIMIT 1"));
    }
}
