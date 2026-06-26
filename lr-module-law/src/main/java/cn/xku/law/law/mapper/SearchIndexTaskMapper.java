package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.SearchIndexTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SearchIndexTaskMapper extends BaseMapper<SearchIndexTaskDO> {

    /** 原子领取：CAS 更新 pending→processing，返回影响行数（0=已被抢占，1=领取成功） */
    @Update("UPDATE lr_search_index_task SET sync_status='processing', update_time=NOW() " +
            "WHERE id=#{id} AND sync_status='pending' AND deleted=0")
    int claimTask(@Param("id") Long id);

    /** 统计指定 ref 在途（pending/processing）的 delete 补偿任务数，用于创建前去重。 */
    @Select("SELECT COUNT(*) FROM lr_search_index_task " +
            "WHERE ref_type=#{refType} AND ref_id=#{refId} AND action_type='delete' " +
            "AND sync_status IN ('pending','processing') AND deleted=0")
    int countActiveDeleteTask(@Param("refType") String refType, @Param("refId") Long refId);

    /**
     * 存量回填：为「已发布、且为所属文档当前版本、且无在途 upsert 任务」的版本批量入队一条 pending upsert。
     * 仅纳入 published + current 版本，与检索结果回查口径一致（其余命中本就会被剔脏/补偿删除）。
     * 单条 INSERT...SELECT，避免把 2 万+ 版本读进内存；涉及表均在租户白名单内，不被注入 tenant_id。
     * 返回入队条数。重复调用幂等（已有在途 upsert 的版本被 NOT EXISTS 排除）。
     */
    @Insert("INSERT INTO lr_search_index_task " +
            "(ref_type, ref_id, index_name, action_type, sync_status, retry_count, create_time, update_time, deleted, tenant_id) " +
            "SELECT 'law_version', v.id, 'law_document', 'upsert', 'pending', 0, NOW(), NOW(), b'0', 0 " +
            "FROM lr_law_version v " +
            "JOIN lr_law_document d ON d.id = v.document_id AND d.current_version_id = v.id AND d.deleted=0 " +
            "WHERE v.version_status='published' AND v.deleted=0 " +
            "AND NOT EXISTS (SELECT 1 FROM lr_search_index_task t " +
            "  WHERE t.ref_type='law_version' AND t.ref_id=v.id AND t.action_type='upsert' " +
            "  AND t.sync_status IN ('pending','processing') AND t.deleted=0)")
    int backfillPublishedCurrent();
}
