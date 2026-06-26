package cn.xku.law.collect.source;

import java.util.Map;

/**
 * FLK（国家法律法规数据库）码值映射表。码值与含义经真实样例核实
 * （data-collection/fglaw20260527/laws_metadata.json，29,493 条）。
 *
 * <p>两套码：
 * <ul>
 *   <li>{@code zdjgCodeId} — 制定机关所属省级行政区码，严格一码一省（170 北京 … 470 新疆，9999 杂项）。
 *       用作 {@code region_code}。</li>
 *   <li>{@code flfgCodeId} — 法规分类码（法律/行政法规/地方法规/司法解释…），用作主题领域/分类。
 *       注意：这是“法规性质分类”，<b>不是行业分类</b>。</li>
 * </ul>
 */
public final class FlkCodeTables {

    private FlkCodeTables() {
    }

    /** zdjgCodeId → 省级行政区名称。9999=杂项（机关名跨省/无法归一），不在表内时返回 null。 */
    private static final Map<Integer, String> REGION_BY_ZDJG = Map.ofEntries(
            Map.entry(170, "北京市"),
            Map.entry(180, "天津市"),
            Map.entry(190, "河北省"),
            Map.entry(200, "山西省"),
            Map.entry(210, "内蒙古自治区"),
            Map.entry(220, "辽宁省"),
            Map.entry(230, "吉林省"),
            Map.entry(240, "黑龙江省"),
            Map.entry(250, "上海市"),
            Map.entry(260, "江苏省"),
            Map.entry(270, "浙江省"),
            Map.entry(280, "安徽省"),
            Map.entry(290, "福建省"),
            Map.entry(300, "江西省"),
            Map.entry(310, "山东省"),
            Map.entry(320, "河南省"),
            Map.entry(330, "湖北省"),
            Map.entry(340, "湖南省"),
            Map.entry(350, "广东省"),
            Map.entry(360, "广西壮族自治区"),
            Map.entry(370, "海南省"),
            Map.entry(380, "重庆市"),
            Map.entry(390, "四川省"),
            Map.entry(400, "贵州省"),
            Map.entry(410, "云南省"),
            Map.entry(420, "西藏自治区"),
            Map.entry(430, "陕西省"),
            Map.entry(440, "甘肃省"),
            Map.entry(450, "青海省"),
            Map.entry(460, "宁夏回族自治区"),
            Map.entry(470, "新疆维吾尔自治区")
    );

    /**
     * flfgCodeId → 法规分类名称。覆盖真实样例出现的全部码值；未知码返回 null（不臆造）。
     * 中央立法（100~200 段）多为法律/行政法规/司法解释；地方段（230/270/290/300…）为地方性法规。
     */
    private static final Map<Integer, String> CATEGORY_BY_FLFG = Map.ofEntries(
            Map.entry(100, "宪法"),
            Map.entry(110, "法律"),
            Map.entry(120, "法律"),
            Map.entry(130, "法律"),
            Map.entry(140, "法律"),
            Map.entry(150, "法律"),
            Map.entry(155, "法律"),
            Map.entry(160, "法律"),
            Map.entry(170, "法律"),
            Map.entry(180, "法律解释"),
            Map.entry(190, "有关法律问题和重大问题的决定"),
            Map.entry(195, "法律修正案"),
            Map.entry(200, "修改、废止的决定"),
            Map.entry(210, "行政法规"),
            Map.entry(215, "修改、废止的决定"),
            Map.entry(220, "监察法规"),
            Map.entry(230, "地方性法规"),
            Map.entry(260, "自治条例和单行条例"),
            Map.entry(270, "地方性法规"),
            Map.entry(290, "经济特区法规"),
            Map.entry(295, "浦东新区法规"),
            Map.entry(300, "海南自由贸易港法规"),
            Map.entry(305, "法规性决定"),
            Map.entry(310, "修改、废止的决定"),
            Map.entry(320, "司法解释"),
            Map.entry(330, "司法解释"),
            Map.entry(340, "司法解释"),
            Map.entry(350, "修改、废止的决定")
    );

    /** zdjgCodeId → 省级行政区名；无对应（含 9999）返回 null。 */
    public static String region(Integer zdjgCodeId) {
        if (zdjgCodeId == null) {
            return null;
        }
        return REGION_BY_ZDJG.get(zdjgCodeId);
    }

    /** flfgCodeId → 法规分类名；未知返回 null。 */
    public static String category(Integer flfgCodeId) {
        if (flfgCodeId == null) {
            return null;
        }
        return CATEGORY_BY_FLFG.get(flfgCodeId);
    }
}
