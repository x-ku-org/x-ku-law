package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.LawInterpretationDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LawInterpretationMapper extends BaseMapper<LawInterpretationDO> {

    /**
     * 物理删除某版本的解读（绕过逻辑删除）。保证幂等重跑：逻辑删除会让多条 deleted=1 行在
     * 唯一键 (version_id, deleted) 上冲突，故按版本物理清理后再插入。
     */
    @Delete("DELETE FROM lr_law_interpretation WHERE version_id=#{versionId}")
    int physicalDeleteByVersion(@Param("versionId") Long versionId);
}
