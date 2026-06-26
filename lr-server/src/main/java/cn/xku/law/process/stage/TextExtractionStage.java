package cn.xku.law.process.stage;

import cn.xku.law.common.client.FileStorageClient;
import cn.xku.law.file.FileService;
import cn.xku.law.file.domain.FileObjectDO;
import cn.xku.law.law.domain.LawVersionDO;
import cn.xku.law.law.service.LawVersionService;
import cn.xku.law.process.LawProcessingContext;
import cn.xku.law.process.LawProcessingStage;
import cn.xku.law.process.extract.DocumentTextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 阶段 10：从版本关联的正文文件抽取纯文本，写回 lr_law_version.content_text/content_hash，
 * 并放入上下文供分段阶段消费。无文件（fileId 空）或不支持的类型时跳过（保留仅元数据接入语义）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextExtractionStage implements LawProcessingStage {

    private final FileService fileService;
    private final FileStorageClient fileStorageClient;
    private final DocumentTextExtractor textExtractor;
    private final LawVersionService lawVersionService;

    @Override
    public String name() {
        return "text-extraction";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void process(LawProcessingContext ctx) {
        if (ctx.getFileId() == null) {
            log.debug("[TextExtraction] versionId={} has no file, skip extraction (metadata-only)", ctx.getVersionId());
            return;
        }
        FileObjectDO file = fileService.loadForProcessing(ctx.getFileId());
        if (file == null) {
            log.warn("[TextExtraction] fileId={} not found, skip (versionId={})", ctx.getFileId(), ctx.getVersionId());
            return;
        }
        String ext = file.getFileExt();
        if (!textExtractor.supports(ext)) {
            log.warn("[TextExtraction] unsupported ext '{}' for fileId={}, skip", ext, ctx.getFileId());
            return;
        }

        byte[] bytes = fileStorageClient.download(file.getObjectKey());
        String text = textExtractor.extract(bytes, ext, file.getOriginalName());
        if (!StringUtils.hasText(text)) {
            log.warn("[TextExtraction] empty text extracted from fileId={} (ext={}), versionId={}",
                    ctx.getFileId(), ext, ctx.getVersionId());
            return;
        }

        ctx.setExtractedText(text);
        LawVersionDO version = lawVersionService.getById(ctx.getVersionId());
        if (version != null) {
            version.setContentText(text);
            version.setContentHash(sha256(text));
            lawVersionService.updateById(version);
        }
        log.info("[TextExtraction] versionId={} extracted {} chars from fileId={}",
                ctx.getVersionId(), text.length(), ctx.getFileId());
    }

    private static String sha256(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
