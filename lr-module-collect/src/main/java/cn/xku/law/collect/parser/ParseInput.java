package cn.xku.law.collect.parser;

import lombok.Data;

import java.util.Map;

/**
 * 解析输入。{@code text} 为已抽取的纯文本（有则触发正文解析）；{@code bytes} 为原始文件字节
 * （docx/pdf 等二进制正文抽取属后续扩展，本期不实现）；{@code metadata} 为来源元数据原始行。
 */
@Data
public class ParseInput {

    /** 已抽取的纯文本正文，可为 null */
    private String text;
    /** 原始文件字节，可为 null（二进制正文抽取为后续扩展） */
    private byte[] bytes;
    /** 文件名（含扩展名），可为 null */
    private String fileName;
    /** MIME 类型，可为 null */
    private String mimeType;
    /** 来源元数据原始行 */
    private Map<String, Object> metadata;

    public static ParseInput ofText(String text, Map<String, Object> metadata) {
        ParseInput in = new ParseInput();
        in.text = text;
        in.metadata = metadata;
        return in;
    }

    public static ParseInput metadataOnly(Map<String, Object> metadata) {
        ParseInput in = new ParseInput();
        in.metadata = metadata;
        return in;
    }
}
