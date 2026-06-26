package cn.xku.law.collect.source;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据源适配器注册表：注入所有 {@link SourceAdapter} Bean，按 {@code sourceCode} 解析。
 * 镜像 {@link cn.xku.law.collect.parser.ParserRegistry} 的插拔风格——新增数据源无需改编排器。
 */
@Slf4j
@Component
public class SourceAdapterRegistry {

    private final List<SourceAdapter> adapters;
    private final Map<String, SourceAdapter> byCode;

    public SourceAdapterRegistry(List<SourceAdapter> adapters) {
        this.adapters = adapters;
        this.byCode = adapters.stream()
                .collect(Collectors.toMap(SourceAdapter::sourceCode, Function.identity()));
        log.info("[SourceAdapterRegistry] loaded {} sources: {}", adapters.size(),
                adapters.stream().map(SourceAdapter::sourceCode).toList());
    }

    /** 所有已注册的数据源，供编排器逐个扫描。 */
    public List<SourceAdapter> all() {
        return adapters;
    }

    /** 按源编码解析适配器；未注册返回 null。 */
    public SourceAdapter resolve(String sourceCode) {
        return byCode.get(sourceCode);
    }
}
