package cn.xku.law.law.service;

import cn.xku.law.common.result.PageResult;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.domain.dto.LawVersionCreateDTO;
import cn.xku.law.law.domain.dto.LawVersionQueryDTO;
import cn.xku.law.law.domain.vo.LawVersionVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LawVersionService extends IService<LawVersionDO> {

    PageResult<LawVersionVO> pageVersions(LawVersionQueryDTO query);

    LawVersionVO getVersionById(Long id);

    Long createVersion(LawVersionCreateDTO dto);

    void updateVersion(Long id, LawVersionCreateDTO dto);

    void removeVersion(Long id);

    /**
     * 发布法规版本：versionStatus → published，更新主文档 currentVersionId，
     * 创建搜索/向量同步任务，并发布 LawVersionPublishedEvent 触发订阅预警。
     */
    void publishVersion(Long id);

    /**
     * 同 {@link #publishVersion(Long)}，但可抑制订阅预警。
     * 初始批量导入历史法规时传 {@code true}，避免对全量数据触发海量订阅预警。
     */
    void publishVersion(Long id, boolean suppressSubscriptionAlert);

    /**
     * 为某已发布版本入队一条检索索引 upsert 任务。
     * AI 旁路富集（写回 summary/标签）后调用，使新元数据进入检索引擎。
     */
    void enqueueSearchIndex(Long versionId);
}
