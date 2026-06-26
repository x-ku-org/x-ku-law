package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.LawAiTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LawAiTaskMapper extends BaseMapper<LawAiTaskDO> {

    /** 原子领取：CAS 更新 pending→processing，返回影响行数（0=已被抢占，1=领取成功） */
    @Update("UPDATE lr_law_ai_task SET process_status='processing', started_at=NOW(), update_time=NOW() " +
            "WHERE id=#{id} AND process_status='pending' AND deleted=0")
    int claimTask(@Param("id") Long id);

    /** 统计指定版本在途（pending/processing）的 AI 任务数，用于入队前去重。 */
    @Select("SELECT COUNT(*) FROM lr_law_ai_task " +
            "WHERE version_id=#{versionId} AND process_status IN ('pending','processing') AND deleted=0")
    int countActiveByVersion(@Param("versionId") Long versionId);

    /**
     * 存量回填：为「已发布、且尚无在途/历史 AI 任务、且尚无解读」的版本批量入队一条 pending 任务。
     * 单条 INSERT...SELECT，避免把 2 万+ 版本读进内存；涉及表均在租户白名单内，不被注入 tenant_id。
     * 返回入队条数。重复调用幂等（已入队/已解读的版本被 NOT EXISTS 排除）。
     */
    @Insert("INSERT INTO lr_law_ai_task " +
            "(version_id, document_id, process_status, retry_count, create_time, update_time, deleted, tenant_id) " +
            "SELECT v.id, v.document_id, 'pending', 0, NOW(), NOW(), b'0', 0 " +
            "FROM lr_law_version v " +
            "WHERE v.version_status='published' AND v.deleted=0 " +
            "AND NOT EXISTS (SELECT 1 FROM lr_law_ai_task t WHERE t.version_id=v.id AND t.deleted=0) " +
            "AND NOT EXISTS (SELECT 1 FROM lr_law_interpretation i WHERE i.version_id=v.id AND i.deleted=0)")
    int backfillPublishedMissing();
}
