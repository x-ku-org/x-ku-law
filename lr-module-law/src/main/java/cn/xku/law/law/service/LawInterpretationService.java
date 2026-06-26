package cn.xku.law.law.service;

import cn.xku.law.law.domain.LawInterpretationDO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LawInterpretationService extends IService<LawInterpretationDO> {

    /**
     * 幂等保存某版本的整篇解读：先按版本物理清旧，再插入新结果。
     * 供 InterpretationStage（AI 旁路）调用。
     */
    void saveForVersion(Long documentId, Long versionId, String model, String text);

    /** 取某版本的解读，无则返回 null。 */
    LawInterpretationDO getByVersionId(Long versionId);
}
