package cn.xku.law.law.mapper;

import cn.xku.law.law.domain.CompareRecordDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** lr_compare_record 数据访问层 */
@Mapper
public interface CompareRecordMapper extends BaseMapper<CompareRecordDO> {

    @InterceptorIgnore(tenantLine = "true")
    @Delete("""
            DELETE FROM lr_compare_record
            WHERE tenant_id = 0
              AND user_id = #{userId}
              AND base_version_id = #{baseVersionId}
              AND target_version_id = #{targetVersionId}
            """)
    int deleteSystemDiff(@Param("userId") Long userId,
                         @Param("baseVersionId") Long baseVersionId,
                         @Param("targetVersionId") Long targetVersionId);

    @InterceptorIgnore(tenantLine = "true")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("""
            INSERT INTO lr_compare_record
                (user_id, document_id, base_version_id, target_version_id, compare_type,
                 result_json, file_id, creator, updater, tenant_id)
            VALUES
                (#{userId}, #{documentId}, #{baseVersionId}, #{targetVersionId}, #{compareType},
                 #{resultJson}, #{fileId}, 'system', 'system', 0)
            """)
    int insertSystemDiff(CompareRecordDO record);

    @InterceptorIgnore(tenantLine = "true")
    @Select("""
            SELECT *
            FROM lr_compare_record
            WHERE tenant_id = 0
              AND deleted = 0
              AND user_id = #{userId}
              AND base_version_id = #{baseVersionId}
              AND target_version_id = #{targetVersionId}
            LIMIT 1
            """)
    CompareRecordDO selectSystemDiff(@Param("userId") Long userId,
                                     @Param("baseVersionId") Long baseVersionId,
                                     @Param("targetVersionId") Long targetVersionId);
}
