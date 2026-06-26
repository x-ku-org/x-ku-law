package cn.xku.law.collect.parser;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 兜底解析器：始终适用，不产出正文与条款，parseStatus=metadata_only。
 * 优先级最低（{@link Order} 最大），确保只有在没有更具体解析器时才被选中。
 */
@Component
@Order(Integer.MAX_VALUE)
public class MetadataOnlyParser implements RawDocumentParser {

    @Override
    public String parserCode() {
        return "metadata-only";
    }

    @Override
    public boolean supports(ParseInput input) {
        return true;
    }

    @Override
    public ParseResult parse(ParseInput input) {
        return ParseResult.metadataOnly();
    }
}
