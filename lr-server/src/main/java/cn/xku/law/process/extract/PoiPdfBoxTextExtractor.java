package cn.xku.law.process.extract;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * 基于 Apache POI（docx/doc）与 PDFBox（pdf）的正文抽取实现。
 * 扫描件 PDF 的 OCR 不在本期范围（图片型 PDF 抽取结果可能为空）。
 */
@Slf4j
@Component
public class PoiPdfBoxTextExtractor implements DocumentTextExtractor {

    private static final Set<String> SUPPORTED = Set.of("docx", "doc", "pdf", "txt");

    @Override
    public boolean supports(String ext) {
        return ext != null && SUPPORTED.contains(ext.toLowerCase());
    }

    @Override
    public String extract(byte[] bytes, String ext, String fileName) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        String e = ext == null ? "" : ext.toLowerCase();
        try {
            return switch (e) {
                case "docx" -> extractDocx(bytes);
                case "doc" -> extractDoc(bytes);
                case "pdf" -> extractPdf(bytes);
                case "txt" -> new String(bytes, StandardCharsets.UTF_8);
                default -> {
                    log.warn("[TextExtractor] unsupported ext '{}' for file {}", e, fileName);
                    yield "";
                }
            };
        } catch (Exception ex) {
            // 抽取失败抛出，由阶段判定是否重试/失败；保留文件名便于排查。
            throw new IllegalStateException("文本抽取失败 (" + fileName + "): " + ex.getMessage(), ex);
        }
    }

    private String extractDocx(byte[] bytes) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return normalize(extractor.getText());
        }
    }

    private String extractDoc(byte[] bytes) throws Exception {
        try (HWPFDocument doc = new HWPFDocument(new ByteArrayInputStream(bytes));
             WordExtractor extractor = new WordExtractor(doc)) {
            return normalize(extractor.getText());
        }
    }

    private String extractPdf(byte[] bytes) throws Exception {
        try (PDDocument doc = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return normalize(stripper.getText(doc));
        }
    }

    /** 统一换行、压去多余空白行，保留段落结构（分段解析依赖换行与「第X条」标记）。 */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t\\u00A0]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
