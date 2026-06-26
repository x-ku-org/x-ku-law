package cn.xku.law.collect.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 原始文档（暂存层），对应 lr_raw_document。
 * 采集元数据先落到这里（metadataJson 存整行原始 JSON），再提升为 lr_law_document / lr_law_version。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_raw_document")
public class RawDocumentDO extends BaseDO {

    private Long sourceId;
    private Long collectRecordId;
    private String title;
    private String sourceUrl;
    private String originalDocNo;
    private String publishOrg;
    private LocalDate publishDate;
    /** 内容哈希，去重键（idx_raw_doc_hash） */
    private String contentHash;
    /** 抽取到的正文全文（元数据级接入时为 null） */
    private String rawText;
    private Long rawFileId;
    /** pending/parsed/metadata_only/failed */
    private String parseStatus;
    private String parseError;
    /** 来源元数据原始 JSON（MySQL json 列，存合法 JSON 字符串） */
    private String metadataJson;
}
