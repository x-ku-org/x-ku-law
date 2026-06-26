package cn.xku.law.collect.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 解析器注册表：注入所有 {@link RawDocumentParser}（已按 @Order 排序），
 * 选第一个 {@code supports()} 的解析器；MetadataOnlyParser 因 @Order 最大始终垫底兜底。
 */
@Slf4j
@Component
public class ParserRegistry {

    private final List<RawDocumentParser> parsers;

    public ParserRegistry(List<RawDocumentParser> parsers) {
        this.parsers = parsers;
        log.info("[ParserRegistry] loaded {} parsers: {}", parsers.size(),
                parsers.stream().map(RawDocumentParser::parserCode).toList());
    }

    /** 选择解析器并执行；任意异常降级为 failed，绝不向上抛断流程。 */
    public ParseResult parse(ParseInput input) {
        for (RawDocumentParser parser : parsers) {
            if (parser.supports(input)) {
                try {
                    return parser.parse(input);
                } catch (Exception e) {
                    log.warn("[ParserRegistry] parser {} failed: {}", parser.parserCode(), e.getMessage());
                    return ParseResult.failed(parser.parserCode() + ": " + e.getMessage());
                }
            }
        }
        return ParseResult.metadataOnly();
    }
}
