package cn.xku.law.file.vo;

import lombok.Data;

import java.util.Map;

/** 预签名直传响应:前端据此 PUT 文件,成功后再调 /files/{fileId}/complete。 */
@Data
public class FilePresignVO {

    /** lr_file_object 记录 ID（pending 状态） */
    private Long fileId;

    /** 预分配的对象 Key */
    private String objectKey;

    /** 预签名上传 URL */
    private String uploadUrl;

    /** URL 有效期（秒） */
    private Integer expireSeconds;

    /** 上传方法，固定 PUT */
    private String method;

    /** 前端 PUT 时必须带的请求头（如 Content-Type） */
    private Map<String, String> headers;
}
