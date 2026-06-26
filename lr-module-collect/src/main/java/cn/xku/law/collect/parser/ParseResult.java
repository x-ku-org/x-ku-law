package cn.xku.law.collect.parser;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/** 解析结果。parseStatus 与 lr_raw_document.parse_status 对应：parsed/metadata_only/failed。 */
@Data
public class ParseResult {

    /** 全文，可为 null（仅元数据时） */
    private String fullText;
    private List<ParsedArticle> articles = Collections.emptyList();
    /** parsed / metadata_only / failed */
    private String parseStatus;
    private String parseError;

    public static ParseResult metadataOnly() {
        ParseResult r = new ParseResult();
        r.parseStatus = "metadata_only";
        return r;
    }

    public static ParseResult parsed(String fullText, List<ParsedArticle> articles) {
        ParseResult r = new ParseResult();
        r.fullText = fullText;
        r.articles = articles != null ? articles : Collections.emptyList();
        r.parseStatus = "parsed";
        return r;
    }

    public static ParseResult failed(String error) {
        ParseResult r = new ParseResult();
        r.parseStatus = "failed";
        r.parseError = error;
        return r;
    }
}
