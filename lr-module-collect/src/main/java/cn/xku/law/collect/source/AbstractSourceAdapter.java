package cn.xku.law.collect.source;

import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * {@link SourceAdapter} 的公共基类：提供各源映射常用的字段/日期读取与哈希工具。
 * 这些工具原先散落在 lr-server 的 CollectItemPromoter，集中到此处供各源适配器复用。
 */
public abstract class AbstractSourceAdapter implements SourceAdapter {

    protected static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
    };

    /** 按候选键顺序取第一个非空字符串值（数字等标量转字符串；跳过 Map/集合）。 */
    protected String str(Map<String, Object> item, String... keys) {
        for (String key : keys) {
            Object v = item.get(key);
            if (v instanceof String s && StringUtils.hasText(s)) {
                return s.trim();
            }
            if (v != null && !(v instanceof Map) && !(v instanceof Iterable)
                    && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }

    /** 按候选键取日期，容忍多种格式与时间后缀；无法解析返回 null。 */
    protected LocalDate date(Map<String, Object> item, String... keys) {
        String raw = str(item, keys);
        if (raw == null) {
            return null;
        }
        String s = raw.length() > 10 ? raw.substring(0, 10) : raw;
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(s, fmt);
            } catch (Exception ignored) {
                // try next format
            }
        }
        return null;
    }

    protected static String defaultTitle(String title) {
        return StringUtils.hasText(title) ? title : "(未命名)";
    }

    protected static String sha1(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest((s == null ? "" : s).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-1 unavailable", e);
        }
    }
}
