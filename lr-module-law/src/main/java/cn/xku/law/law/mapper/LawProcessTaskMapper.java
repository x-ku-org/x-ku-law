package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.LawProcessTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LawProcessTaskMapper extends BaseMapper<LawProcessTaskDO> {

    /** 原子领取：CAS 更新 pending→processing，返回影响行数（0=已被抢占，1=领取成功） */
    @Update("UPDATE lr_law_process_task SET process_status='processing', started_at=NOW(), update_time=NOW() " +
            "WHERE id=#{id} AND process_status='pending' AND deleted=0")
    int claimTask(@Param("id") Long id);

    /** 统计指定版本在途（pending/processing）的处理任务数，用于入队前去重，避免重复处理同一版本。 */
    @Select("SELECT COUNT(*) FROM lr_law_process_task " +
            "WHERE version_id=#{versionId} AND process_status IN ('pending','processing') AND deleted=0")
    int countActiveByVersion(@Param("versionId") Long versionId);
}
