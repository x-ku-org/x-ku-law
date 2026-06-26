package cn.xku.law.collect.source;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * 国家法律法规数据库(FLK) 数据源适配器——当前的<b>默认</b>源。
 *
 * <p>字段名已据真实样例核实（data-collection/fglaw20260527/laws_metadata.json，29,493 条，字段 100% 填充）：
 * {@code title}=标题, {@code gbrq}=公布日, {@code sxrq}=施行日, {@code sxx}=时效性(整数),
 * {@code zdjgName}=制定机关, {@code flxz}=法律性质/效力级别。<b>无文号、无来源 URL</b>；
 * {@code bbbs} 是 FLK 自家库的版本 GUID，<b>刻意不使用</b>（不依赖外部库主键）。
 */
@Component
public class FlkSourceAdapter extends AbstractSourceAdapter {

    private static final String[] DOC_EXTS = {"docx", "doc", "pdf"};

    @Override
    public String sourceCode() {
        return "flk";
    }

    @Override
    public String folderPrefix() {
        return "fglaw";
    }

    @Override
    public String metadataFileName() {
        return "laws_metadata.json";
    }

    @Override
    public String sourceName() {
        return "国家法律法规数据库(FLK)";
    }

    @Override
    public MappedLaw map(Map<String, Object> item) {
        String title = str(item, "title");
        String org = str(item, "zdjgName");
        LocalDate publishDate = date(item, "gbrq");
        LocalDate effectiveDate = date(item, "sxrq");
        String legalLevel = str(item, "flxz");

        // 不用 bbbs（外部库 GUID）；用 归一化标题 + 制定机关，版本无关 → 跨版本稳定。
        String lawUid = "FLK:" + sha1(TitleNormalizer.normalize(title) + "|"
                + (org == null ? "" : org));
        // 版本键：同一 lawUid 下用公布日区分；缺失则降级为 "0"（同法的多个无日期版本会并为一版）。
        String versionKey = publishDate != null ? publishDate.format(YMD) : "0";

        String regionCode = FlkCodeTables.region(toInt(item.get("zdjgCodeId")));
        String subjectDomain = FlkCodeTables.category(toInt(item.get("flfgCodeId")));

        return new MappedLaw(lawUid, versionKey, defaultTitle(title), null, org,
                publishDate, effectiveDate, mapStatus(item.get("sxx")), "law", legalLevel, null,
                regionCode, subjectDomain, null);
    }

    /**
     * FLK 文件命名为 {@code {标题}_{公布日yyyyMMdd}.{docx|doc|pdf}}；无公布日时为 {@code {标题}_.ext} 或 {@code {标题}.ext}。
     * 仅做 O(1) 精确候选匹配，命中返回对象 key；不做模糊/前缀扫描（多候选无法按日期消歧 → 宁可不挂）。
     */
    @Override
    public String matchFileKey(MappedLaw m, String folder, Set<String> keys) {
        String title = m.title();
        if (!StringUtils.hasText(title)) {
            return null;
        }
        if (m.publishDate() != null) {
            String d = m.publishDate().format(YMD);
            for (String ext : DOC_EXTS) {
                String cand = folder + title + "_" + d + "." + ext;
                if (keys.contains(cand)) {
                    return cand;
                }
            }
            return null;
        }
        // 无公布日
        for (String ext : DOC_EXTS) {
            String c1 = folder + title + "_." + ext;
            if (keys.contains(c1)) {
                return c1;
            }
            String c2 = folder + title + "." + ext;
            if (keys.contains(c2)) {
                return c2;
            }
        }
        return null;
    }

    /**
     * FLK 时效性 sxx（整数）→ 时效状态。映射经真实样例反推确认（2026-05-27）：
     * 3=现行有效，2=已修改（被新版取代的历史版本），1=已废止，4=尚未生效（施行日在未来），
     * -1=已失效（边缘），null/其它=未知（如修改/废止决定本身无时效性）。
     */
    static String mapStatus(Object sxxRaw) {
        Integer code = toInt(sxxRaw);
        if (code == null) {
            return "unknown";
        }
        return switch (code) {
            case 3 -> "effective";
            case 2 -> "amended";
            case 1 -> "repealed";
            case 4 -> "not_effective";
            case -1 -> "expired";
            default -> "unknown";
        };
    }

    private static Integer toInt(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        if (v instanceof String s && StringUtils.hasText(s)) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
