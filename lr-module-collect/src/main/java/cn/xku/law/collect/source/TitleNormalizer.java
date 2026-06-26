package cn.xku.law.collect.source;

import org.springframework.util.StringUtils;

/**
 * 标题归一化：用于「同一部法律」的分组键（law_uid）。
 * 不同采集批次/页面对同一标题可能带有细微差异（首尾 '+'、全半角空格、书名号变体），
 * 归一化后再参与分组，避免同一部法律被拆成多条文档。
 *
 * <p>注意：本类只服务于<b>分组键</b>。文件名匹配仍用原始标题（磁盘文件名保留原样）。
 */
public final class TitleNormalizer {

    private TitleNormalizer() {
    }

    /**
     * 归一化标题：trim → 去首尾 '+' → 去除所有空白（含全角空格） → 书名号变体统一为《》。
     * 返回空串当输入为空。
     */
    public static String normalize(String title) {
        if (!StringUtils.hasText(title)) {
            return "";
        }
        String s = title.trim();
        s = s.replaceAll("^[+＋\\s]+", "").replaceAll("[+＋\\s]+$", "");
        s = s.replaceAll("[\\s\\u3000]+", "");
        s = s.replace('〈', '《').replace('〉', '》');
        return s;
    }
}
