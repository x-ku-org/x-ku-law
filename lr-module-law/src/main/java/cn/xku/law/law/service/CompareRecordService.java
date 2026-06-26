package cn.xku.law.law.service;

import cn.xku.law.law.diff.VersionDiffResult;
import cn.xku.law.law.domain.CompareRecordDO;
import com.baomidou.mybatisplus.extension.service.IService;

/** 版本对比记录服务。承载管线自动对比（系统）与用户主动对比的落库与读取。 */
public interface CompareRecordService extends IService<CompareRecordDO> {

    /** 系统用户ID：管线自动生成的对比记录归属此 ID。 */
    long SYSTEM_USER_ID = 0L;

    /**
     * 以系统身份（userId=0）保存一对版本的逐条对比结果，幂等：先删同 (base,target) 的旧系统记录再插。
     *
     * @return 新记录 ID
     */
    Long saveSystemDiff(Long documentId, VersionDiffResult diff);

    /**
     * 取某对版本的系统对比记录；不存在返回 null。
     */
    CompareRecordDO findSystemDiff(Long baseVersionId, Long targetVersionId);

    /**
     * 读取或实时计算一对版本的逐条对比结果（供对比查询接口用）：
     * 命中系统对比记录直接反序列化返回；未命中则实时调 {@code ArticleDiffService} 计算
     * 并落库后返回。结果对前端只读，不依赖管线是否跑过。
     */
    VersionDiffResult getOrComputeDiff(Long documentId, Long baseVersionId, Long targetVersionId);
}
