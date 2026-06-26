package cn.xku.law.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 申请预签名直传请求体 */
@Data
public class FilePresignDTO {

    @NotBlank(message = "原始文件名不能为空")
    private String originalName;

    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须大于 0")
    private Long fileSize;

    /** 关联业务类型，用于组织存储目录，可空（默认 misc） */
    private String refType;
}
