package cn.xku.law.collect.parser;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 纯文本条款解析器：当输入带有纯文本正文时，按法规条款起始标记切分为扁平条款。
 * 仅处理易解析的纯文本；docx/pdf/HTML 等需先抽取为文本（后续扩展），本解析器不触碰二进制。
 */
@Component
@Order(100)
public class PlainTextArticleParser implements RawDocumentParser {

    /** 条款起始标记：第一条 / 第 12 条 / 第二十三条 / 第十条之一 等。 */
    private static final Pattern ARTICLE_MARKER =
            Pattern.compile("(第\\s*[零一二三四五六七八九十百千两0-9]+\\s*条\\s*(?:之\\s*[零一二三四五六七八九十百千两0-9]+)?)");

    /** 末尾章节标题，如“。 第四章 法律责任”，不应归入上一条内容。 */
    private static final Pattern TRAILING_CHAPTER =
            Pattern.compile("\\s*第\\s*[零一二三四五六七八九十百千两0-9]+\\s*[章节编部篇][\\s　\\u4e00-\\u9fa5]*$");

    private static final String[] REFERENCE_PREFIXES = {
            "本法", "本条例", "本规定", "本办法", "本细则", "本规则",
            "依据", "根据", "按照", "依照", "参照",
            "违反", "适用", "援引", "引用", "规定的",
            "前款", "上述", "上列", "该", "同法"
    };

    @Override
    public String parserCode() {
        return "plain-text";
    }

    @Override
    public boolean supports(ParseInput input) {
        return input != null && StringUtils.hasText(input.getText());
    }

    @Override
    public ParseResult parse(ParseInput input) {
        String text = input.getText();
        List<ParsedArticle> articles = splitArticles(text);
        return ParseResult.parsed(text, articles);
    }

    private List<ParsedArticle> splitArticles(String text) {
        List<ParsedArticle> articles = new ArrayList<>();
        Matcher m = ARTICLE_MARKER.matcher(text);
        List<int[]> spans = new ArrayList<>();
        List<String> nos = new ArrayList<>();
        while (m.find()) {
            if (isReferenceMatch(text, m.start())) {
                continue;
            }
            spans.add(new int[]{m.start(), m.end()});
            nos.add(m.group(1).replaceAll("\\s+", ""));
        }
        if (spans.isEmpty()) {
            return articles;
        }

        // 真实条号一定单调递增（第三条必在第二条与第四条之间）。正文里前缀逃过 isReferenceMatch 的
        // 句中交叉引用（如「…不符合第五十三条规定的…」夹在第七十条里）会被误切成新条款，破坏递增。
        // 取按条号数值「严格递增的最长子序列」作为真实条款边界，其余标记判为误切引用；切分内容时
        // 只在被接受的边界处下刀，被丢弃标记的文本自然并回它所在的上一条（还原拼接）。
        boolean[] accepted = selectMonotonicMarkers(nos);
        List<Integer> acceptedIdx = new ArrayList<>();
        for (int i = 0; i < spans.size(); i++) {
            if (accepted[i]) {
                acceptedIdx.add(i);
            }
        }

        int order = 0;
        for (int k = 0; k < acceptedIdx.size(); k++) {
            int i = acceptedIdx.get(k);
            int contentStart = spans.get(i)[0];
            int contentEnd = (k + 1 < acceptedIdx.size()) ? spans.get(acceptedIdx.get(k + 1))[0] : text.length();
            String content = cleanArticleContent(text.substring(contentStart, contentEnd));
            if (k == acceptedIdx.size() - 1 && isIncompleteArticle(content)) {
                continue;
            }
            if (content.length() >= 10) {
                articles.add(new ParsedArticle(nos.get(i), null, content, ++order, 1));
            }
        }
        return articles;
    }

    /**
     * 在所有条款标记中选出「条号数值严格递增的最长子序列」作为真实条款边界。
     * 用最长递增子序列（LIS）而非简单贪心，是为兼容「靠前出现一个数值偏大的误引用」会错杀
     * 后面真实低值条款的情形；严格递增同时天然丢弃完全重复的条号（如两个第五十三条只留其一）。
     * 标记数通常几十到几百，O(n²) DP + 父指针重建足矣。条号无法解析（理论上正则已保证有数字）
     * 时给哨兵值并强制保留为边界，宁可多切不可丢条。
     */
    private static boolean[] selectMonotonicMarkers(List<String> nos) {
        int n = nos.size();
        long[] values = new long[n];
        boolean[] accepted = new boolean[n];
        for (int i = 0; i < n; i++) {
            values[i] = parseArticleValue(nos.get(i));
            if (values[i] < 0) {
                accepted[i] = true; // 解析失败：保守保留为边界
            }
        }

        int[] dp = new int[n];
        int[] parent = new int[n];
        int best = -1;
        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            parent[i] = -1;
            if (values[i] < 0) {
                continue; // 哨兵不参与递增链
            }
            for (int j = 0; j < i; j++) {
                if (values[j] >= 0 && values[j] < values[i] && dp[j] + 1 > dp[i]) {
                    dp[i] = dp[j] + 1;
                    parent[i] = j;
                }
            }
            if (best < 0 || dp[i] > dp[best]) {
                best = i;
            }
        }
        for (int k = best; k >= 0; k = parent[k]) {
            accepted[k] = true;
        }
        return accepted;
    }

    /** 把条款号解析为可比较数值：major*10000 + minor（「之X」进 minor，使 第十条 < 第十条之一 < 第十一条）。无法解析返回 -1。 */
    private static long parseArticleValue(String articleNo) {
        if (articleNo == null) {
            return -1;
        }
        String s = articleNo.replace("第", "");
        int tiaoIdx = s.indexOf('条');
        if (tiaoIdx <= 0) {
            return -1;
        }
        long major = cnNum(s.substring(0, tiaoIdx));
        if (major < 0) {
            return -1;
        }
        long minor = 0;
        int zhiIdx = s.indexOf('之', tiaoIdx);
        if (zhiIdx >= 0 && zhiIdx + 1 < s.length()) {
            long mn = cnNum(s.substring(zhiIdx + 1));
            if (mn > 0) {
                minor = mn;
            }
        }
        return major * 10000L + minor;
    }

    /** 解析中文/阿拉伯数字（零一二三四五六七八九十百千万两，及 0-9）；无法解析返回 -1。 */
    private static long cnNum(String s) {
        if (s == null || s.isEmpty()) {
            return -1;
        }
        boolean allDigits = true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                allDigits = false;
                break;
            }
        }
        if (allDigits) {
            return Long.parseLong(s);
        }
        long total = 0;
        long section = 0;
        long number = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            int d = cnDigit(ch);
            if (d >= 0) {
                number = d;
                continue;
            }
            long unit = cnUnit(ch);
            if (unit == 0) {
                return -1; // 未知字符
            }
            if (unit == 10000) {
                section = (section + number) * unit;
                total += section;
                section = 0;
            } else {
                if (number == 0) {
                    number = 1; // 「十三」中的「十」= 10
                }
                section += number * unit;
            }
            number = 0;
        }
        return total + section + number;
    }

    private static int cnDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        switch (c) {
            case '零': return 0;
            case '一': return 1;
            case '二': case '两': return 2;
            case '三': return 3;
            case '四': return 4;
            case '五': return 5;
            case '六': return 6;
            case '七': return 7;
            case '八': return 8;
            case '九': return 9;
            default: return -1;
        }
    }

    private static long cnUnit(char c) {
        switch (c) {
            case '十': return 10;
            case '百': return 100;
            case '千': return 1000;
            case '万': return 10000;
            default: return 0;
        }
    }

    private boolean isReferenceMatch(String text, int matchStart) {
        if (matchStart == 0) {
            return false;
        }

        char prev = text.charAt(matchStart - 1);
        if (prev == '、' || prev == '，' || prev == ',') {
            return true;
        }

        int lookbackStart = Math.max(0, matchStart - 10);
        String prefix = text.substring(lookbackStart, matchStart);
        for (String refPrefix : REFERENCE_PREFIXES) {
            if (prefix.endsWith(refPrefix)) {
                return true;
            }
        }

        return prefix.endsWith("和")
                || prefix.endsWith("及")
                || prefix.endsWith("与")
                || prefix.endsWith("或者")
                || prefix.endsWith("或");
    }

    private String cleanArticleContent(String content) {
        if (content == null) {
            return "";
        }
        String cleaned = content.replaceAll("\\s+", " ").trim();
        return TRAILING_CHAPTER.matcher(cleaned).replaceAll("").trim();
    }

    private boolean isIncompleteArticle(String content) {
        if (content == null || content.trim().length() < 10) {
            return true;
        }
        String trimmed = content.trim();
        char last = trimmed.charAt(trimmed.length() - 1);
        return last != '。'
                && last != '；'
                && last != '：'
                && last != '"'
                && last != ')'
                && last != '）';
    }
}
