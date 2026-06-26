package cn.xku.law.law.serviceImpl;

import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.mapper.LawDocumentMapper;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 列表/选择器外键可读化的小工具：按法规 ID 批量取标题。
 * 把"分页后回填冗余名"的重复逻辑集中一处，供版本/条款/关系等列表服务复用。
 */
final class LawTitleHelper {

    private LawTitleHelper() {
    }

    /** 按一组法规 ID 批量查询标题（去空、去重）；无有效 ID 时返回空 Map。 */
    static Map<Long, String> titlesByIds(LawDocumentMapper mapper, Collection<Long> ids) {
        Set<Long> clean = ids.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (clean.isEmpty()) {
            return Map.of();
        }
        return mapper.selectBatchIds(clean).stream()
                .collect(Collectors.toMap(
                        LawDocumentDO::getId,
                        d -> d.getTitle() == null ? "" : d.getTitle(),
                        (a, b) -> a));
    }
}
