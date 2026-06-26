package cn.xku.law.collect.mapper;

import cn.xku.law.collect.domain.CollectRecordDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/** lr_collect_record 数据访问层 */
@Mapper
public interface CollectRecordMapper extends BaseMapper<CollectRecordDO> {

    /** 原子领取：CAS 更新 pending→processing，返回影响行数（0=已被抢占，1=领取成功） */
    @Update("UPDATE lr_collect_record SET collect_status='processing', update_time=NOW() " +
            "WHERE id=#{id} AND collect_status='pending' AND deleted=0")
    int claimRecord(@Param("id") Long id);
}
