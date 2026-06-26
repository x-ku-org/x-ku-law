package cn.xku.law.file.mapper;

import cn.xku.law.file.domain.FileObjectDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;

/** lr_file_object 数据访问层 */
@Mapper
public interface FileObjectMapper extends BaseMapper<FileObjectDO> {

    /**
     * 跨租户读取共享法规文件。ingest 链路在无 SecurityContext 时以 tenant_id=0 落库
     * （平台公共数据），而 lr_file_object 默认受行级租户过滤，租户用户（tenant_id≥1）
     * 用自身 tenant_id 查不到这些行。故此处显式绕过 TenantLineHandler 再查一次。
     * <p>安全约束：仅匹配共享 refType（law_document/law_version/raw_document），
     * 绝不会返回任何租户私有上传文件；授权仍由 Service 层 checkReadable 把关。
     * <p>@Select 绕过了逻辑删除插件，故手动追加 deleted=0。
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("<script>SELECT * FROM lr_file_object WHERE id = #{id} AND tenant_id = 0 AND deleted = 0 "
            + "AND ref_type IN "
            + "<foreach collection='refTypes' item='t' open='(' separator=',' close=')'>#{t}</foreach> "
            + "LIMIT 1</script>")
    FileObjectDO selectSharedById(@Param("id") Long id, @Param("refTypes") Collection<String> refTypes);

    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM lr_file_object WHERE object_key = #{objectKey} AND deleted = 0 LIMIT 1")
    FileObjectDO selectByObjectKey(@Param("objectKey") String objectKey);

    @InterceptorIgnore(tenantLine = "true")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("""
            INSERT INTO lr_file_object
                (file_name, original_name, file_ext, mime_type, storage_type, bucket_name,
                 object_key, file_size, sha256, ref_type, status, creator, updater, tenant_id)
            VALUES
                (#{fileName}, #{originalName}, #{fileExt}, #{mimeType}, #{storageType}, #{bucketName},
                 #{objectKey}, #{fileSize}, #{sha256}, #{refType}, #{status}, #{creator}, #{updater}, 0)
            """)
    int insertShared(FileObjectDO entity);

    @InterceptorIgnore(tenantLine = "true")
    @Update("<script>UPDATE lr_file_object "
            + "SET tenant_id = 0, ref_type = #{targetRefType}, updater = #{username}, update_time = NOW() "
            + "WHERE id = #{id} AND deleted = 0 AND status = 'normal' "
            + "AND tenant_id = #{tenantId} AND creator = #{username} "
            + "AND ref_type IN "
            + "<foreach collection='sourceRefTypes' item='t' open='(' separator=',' close=')'>#{t}</foreach>"
            + "</script>")
    int promoteSharedLawFile(@Param("id") Long id,
                             @Param("tenantId") Long tenantId,
                             @Param("username") String username,
                             @Param("targetRefType") String targetRefType,
                             @Param("sourceRefTypes") Collection<String> sourceRefTypes);
}
