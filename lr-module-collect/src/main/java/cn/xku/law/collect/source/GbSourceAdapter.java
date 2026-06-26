package cn.xku.law.collect.source;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * 国家标准全文公开系统(OpenSTD) 数据源适配器。字段名由 openstd_spider/schema.py 确认。
 * GB 各版本标准有各自的 std_code/年份，故按 std_code 各成一篇文档（不跨版本归并）。
 */
@Component
public class GbSourceAdapter extends AbstractSourceAdapter {

    @Override
    public String sourceCode() {
        return "gb";
    }

    @Override
    public String folderPrefix() {
        return "gblaw";
    }

    @Override
    public String metadataFileName() {
        return "standards_metadata.json";
    }

    @Override
    public String sourceName() {
        return "国家标准全文公开系统(OpenSTD)";
    }

    @Override
    public MappedLaw map(Map<String, Object> item) {
        String stdCode = str(item, "std_code");
        String name = str(item, "name_cn", "name");
        LocalDate publishDate = date(item, "pub_date");
        LocalDate effectiveDate = date(item, "impl_date");

        String lawUid = "GB:" + (StringUtils.hasText(stdCode) ? stdCode : sha1(defaultTitle(name)));
        String versionKey = publishDate != null ? publishDate.format(YMD) : "0";

        // 国标无地区维度（全国）。主题领域取标准类型（由 std_code 前缀判定）。
        String subjectDomain = standardType(stdCode);
        String industryCode = str(item, "ics");

        return new MappedLaw(lawUid, versionKey, defaultTitle(name), stdCode, null,
                publishDate, effectiveDate, mapStatus(str(item, "status")),
                "standard", "national_standard", null, null, subjectDomain, industryCode);
    }

    /**
     * 由 std_code 前缀判定标准类型：GB=强制性国家标准，GB/T=推荐性国家标准，GB/Z=国家标准化指导性技术文件。
     * 注意前缀判断顺序：先判更长的 GB/T、GB/Z，再判 GB。无法识别返回 null。
     */
    static String standardType(String stdCode) {
        if (!StringUtils.hasText(stdCode)) {
            return null;
        }
        String s = stdCode.trim();
        if (s.startsWith("GB/T")) {
            return "推荐性国家标准";
        }
        if (s.startsWith("GB/Z")) {
            return "国家标准化指导性技术文件";
        }
        if (s.startsWith("GB")) {
            return "强制性国家标准";
        }
        return null;
    }

    /**
     * GB 正文文件名为 {@code std_code.replace("/","") + ".pdf"}（见 openstd_spider cli.py:347）；
     * 含 '/' 的编号（如 GB/T）在磁盘已被去斜杠，匹配前必须同样处理。
     */
    @Override
    public String matchFileKey(MappedLaw m, String folder, Set<String> keys) {
        if (!StringUtils.hasText(m.docNo())) {
            return null;
        }
        String cand = folder + m.docNo().replace("/", "") + ".pdf";
        return keys.contains(cand) ? cand : null;
    }

    private static String mapStatus(String raw) {
        if (raw == null) {
            return "unknown";
        }
        return switch (raw.toUpperCase()) {
            case "PUBLISHED" -> "effective";
            case "TOBEIMP" -> "not_effective";
            case "WITHDRAWN" -> "repealed";
            default -> "unknown";
        };
    }
}
