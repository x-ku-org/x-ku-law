package cn.xku.law.collect.parser;

/**
 * 原始文档解析器。可插拔：实现类声明能处理的输入，由 {@link ParserRegistry} 选择。
 * 本期内置 {@link MetadataOnlyParser}（兜底）与 {@link PlainTextArticleParser}（纯文本拆条）。
 * 后续扩展点：docx/pdf 正文抽取、扫描件 OCR、章节层级——实现本接口并注册为 Spring Bean 即可。
 */
public interface RawDocumentParser {

    /** 解析器编码，便于日志与配置选择 */
    String parserCode();

    /** 是否能处理该输入 */
    boolean supports(ParseInput input);

    /** 执行解析 */
    ParseResult parse(ParseInput input);
}
