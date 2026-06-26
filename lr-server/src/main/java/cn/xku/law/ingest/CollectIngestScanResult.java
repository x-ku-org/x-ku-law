package cn.xku.law.ingest;

import java.util.List;

/** 一次采集扫描的执行摘要（定时任务与手动触发共用） */
public record CollectIngestScanResult(
        List<SourceScanResult> sources,
        int foldersProcessed) {

    /** 单个数据源的扫描结果 */
    public record SourceScanResult(
            String sourceCode,
            int foldersProcessed,
            String message) {
    }
}
