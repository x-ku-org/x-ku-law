package cn.xku.law.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 文件对象响应视图 */
@Data
public class FileObjectVO {

    private Long id;
    private String originalName;
    private String fileExt;
    private String mimeType;
    private String objectKey;
    private Long fileSize;
    private String sha256;
    private String refType;
    private Long refId;
    private String status;

    /** 临时访问 URL（私有桶为预签名 GET，公开桶为固定 URL） */
    private String accessUrl;

    private LocalDateTime createTime;
}
