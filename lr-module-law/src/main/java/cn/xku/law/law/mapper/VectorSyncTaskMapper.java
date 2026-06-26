package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.VectorSyncTaskDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface VectorSyncTaskMapper extends BaseMapper<VectorSyncTaskDO> {

    /** 原子领取：CAS 更新 pending→processing，返回影响行数（0=已被抢占，1=领取成功） */
    @Update("UPDATE lr_vector_sync_task SET sync_status='processing', update_time=NOW() " +
            "WHERE id=#{id} AND sync_status='pending' AND deleted=0")
    int claimTask(@Param("id") Long id);
}
