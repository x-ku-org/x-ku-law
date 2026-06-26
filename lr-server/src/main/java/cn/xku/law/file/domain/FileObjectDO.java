package cn.xku.law.file.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文件对象，对应 lr_file_object。前端直传时先 pending，complete 后置 normal。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_file_object")
public class FileObjectDO extends BaseDO {

    /** 存储文件名（objectKey 末段） */
    private String fileName;

    /** 原始文件名 */
    private String originalName;

    /** 文件扩展名 */
    private String fileExt;

    /** MIME 类型 */
    private String mimeType;

    /** 存储类型：local/oss/minio/s3 */
    private String storageType;

    /** 桶名称 */
    private String bucketName;

    /** 对象存储 Key */
    private String objectKey;

    /** 访问 URL（公开桶时填固定 URL；私有桶留空，按需签发） */
    private String fileUrl;

    /** 文件大小字节（presign 时为申报值，complete 后为真实值） */
    private Long fileSize;

    /** 文件 SHA256 / ETag */
    private String sha256;

    /** 关联对象类型 */
    private String refType;

    /** 关联对象 ID */
    private Long refId;

    /** 状态：pending（待上传）/normal（已完成） */
    private String status;
}
