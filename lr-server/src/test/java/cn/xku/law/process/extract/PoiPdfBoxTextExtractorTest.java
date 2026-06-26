package cn.xku.law.process.extract;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** PoiPdfBoxTextExtractor 单测：docx / pdf / txt 往返抽取（无 Spring）。 */
class PoiPdfBoxTextExtractorTest {

    private final PoiPdfBoxTextExtractor extractor = new PoiPdfBoxTextExtractor();

    @Test
    void supports_onlyKnownExtensions() {
        assertThat(extractor.supports("docx")).isTrue();
        assertThat(extractor.supports("PDF")).isTrue();
        assertThat(extractor.supports("xlsx")).isFalse();
        assertThat(extractor.supports(null)).isFalse();
    }

    @Test
    void extractDocx_returnsParagraphText() throws Exception {
        byte[] bytes;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText("第一条 国家保护公民的合法权益。");
            doc.createParagraph().createRun().setText("第二条 本法自公布之日起施行。");
            doc.write(out);
            bytes = out.toByteArray();
        }
        String text = extractor.extract(bytes, "docx", "sample.docx");
        assertThat(text).contains("第一条").contains("第二条").contains("施行");
    }

    @Test
    void extractPdf_returnsPageText() throws Exception {
        byte[] bytes;
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(72, 700);
                cs.showText("Article 1 hello world");
                cs.endText();
            }
            doc.save(out);
            bytes = out.toByteArray();
        }
        String text = extractor.extract(bytes, "pdf", "sample.pdf");
        assertThat(text).contains("hello world");
    }

    @Test
    void extractTxt_returnsRawText() {
        byte[] bytes = "第一条 测试条文。".getBytes(StandardCharsets.UTF_8);
        String text = extractor.extract(bytes, "txt", "a.txt");
        assertThat(text).contains("测试条文");
    }

    @Test
    void extract_emptyBytes_returnsEmpty() {
        assertThat(extractor.extract(new byte[0], "pdf", "x.pdf")).isEmpty();
    }
}
