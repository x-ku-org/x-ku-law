package cn.xku.law.collect.source;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FlkSourceAdapterTest {

    private final FlkSourceAdapter adapter = new FlkSourceAdapter();

    private static Map<String, Object> record(String title, String org, String gbrq, String sxrq, Object sxx, String flxz) {
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("zdjgName", org);
        m.put("gbrq", gbrq);
        m.put("sxrq", sxrq);
        m.put("sxx", sxx);
        m.put("flxz", flxz);
        return m;
    }

    @Test
    void sameLawDifferentVersionsShareLawUidButDifferVersionKey() {
        MappedLaw v2013 = adapter.map(record("中华人民共和国公司法", "全国人民代表大会常务委员会",
                "2013-12-28", "2014-03-01", 2, "法律"));
        MappedLaw v2018 = adapter.map(record("中华人民共和国公司法", "全国人民代表大会常务委员会",
                "2018-10-26", "2018-10-26", 2, "法律"));
        MappedLaw v2023 = adapter.map(record("中华人民共和国公司法", "全国人民代表大会常务委员会",
                "2023-12-29", "2024-07-01", 3, "法律"));

        // 同一部法律 → 同 lawUid（归并到一个文档）
        assertThat(v2013.lawUid()).isEqualTo(v2018.lawUid()).isEqualTo(v2023.lawUid());
        assertThat(v2013.lawUid()).startsWith("FLK:");
        // 版本键各异（公布日 yyyyMMdd）
        assertThat(v2013.versionKey()).isEqualTo("20131228");
        assertThat(v2018.versionKey()).isEqualTo("20181026");
        assertThat(v2023.versionKey()).isEqualTo("20231229");
        // 状态映射
        assertThat(v2013.status()).isEqualTo("amended");
        assertThat(v2023.status()).isEqualTo("effective");
        assertThat(v2023.legalLevel()).isEqualTo("法律");
        assertThat(v2023.issuingOrg()).isEqualTo("全国人民代表大会常务委员会");
    }

    @Test
    void lawUidStableAcrossTitleNoise() {
        // 标题带前缀 '+'、全角空格、〈〉变体 → 归一化后应与干净标题同组
        String clean = adapter.map(record("某省实施《中华人民共和国X法》办法", "某省人大常委会",
                "2020-01-01", "2020-02-01", 3, "地方性法规")).lawUid();
        String noisy = adapter.map(record("＋某省实施〈中华人民共和国X法〉办法 ", "某省人大常委会",
                "2021-01-01", "2021-02-01", 3, "地方性法规")).lawUid();
        assertThat(noisy).isEqualTo(clean);
    }

    @Test
    void mapsRegionAndSubjectFromCodes() {
        // zdjgCodeId=350→广东省, flfgCodeId=230→地方性法规
        Map<String, Object> local = record("某市某条例", "广东省人民代表大会常务委员会",
                "2022-01-01", "2022-03-01", 3, "地方性法规");
        local.put("zdjgCodeId", 350);
        local.put("flfgCodeId", 230);
        MappedLaw m = adapter.map(local);
        assertThat(m.regionCode()).isEqualTo("广东省");
        assertThat(m.subjectDomain()).isEqualTo("地方性法规");

        Map<String, Object> central = record("某条例", "国务院",
                "2022-01-01", "2022-03-01", 3, "行政法规");
        central.put("zdjgCodeId", 120);
        central.put("flfgCodeId", 210);
        MappedLaw c = adapter.map(central);
        assertThat(c.regionCode()).isNull();
        assertThat(c.subjectDomain()).isEqualTo("行政法规");

        // 9999 杂项 → region 为 null（不臆造）
        Map<String, Object> misc = record("某决定", "某机关", "2022-01-01", "2022-03-01", 1, "");
        misc.put("zdjgCodeId", 9999);
        assertThat(adapter.map(misc).regionCode()).isNull();
    }

    @Test
    void sxxMapsToStatus() {
        assertThat(FlkSourceAdapter.mapStatus(3)).isEqualTo("effective");
        assertThat(FlkSourceAdapter.mapStatus(2)).isEqualTo("amended");
        assertThat(FlkSourceAdapter.mapStatus(1)).isEqualTo("repealed");
        assertThat(FlkSourceAdapter.mapStatus(4)).isEqualTo("not_effective");
        assertThat(FlkSourceAdapter.mapStatus(-1)).isEqualTo("expired");
        assertThat(FlkSourceAdapter.mapStatus(null)).isEqualTo("unknown");
        assertThat(FlkSourceAdapter.mapStatus(99)).isEqualTo("unknown");
        assertThat(FlkSourceAdapter.mapStatus("3")).isEqualTo("effective"); // 字符串也容忍
    }

    @Test
    void matchFileKeyHitsExactDateFile() {
        MappedLaw m = adapter.map(record("中华人民共和国公司法", "全国人民代表大会常务委员会",
                "2023-12-29", "2024-07-01", 3, "法律"));
        String folder = "fglaw20260527/";
        Set<String> keys = Set.of(
                folder + "laws_metadata.json",
                folder + "中华人民共和国公司法_20231229.docx",
                folder + "其他法规_20200101.docx");
        assertThat(adapter.matchFileKey(m, folder, keys))
                .isEqualTo(folder + "中华人民共和国公司法_20231229.docx");
    }

    @Test
    void matchFileKeyReturnsNullWhenDateMismatch() {
        MappedLaw m = adapter.map(record("中华人民共和国公司法", "全国人民代表大会常务委员会",
                "2023-12-29", "2024-07-01", 3, "法律"));
        String folder = "fglaw20260527/";
        // 只有别的日期的同名文件 → 无法按公布日确认 → 不挂（返回 null）
        Set<String> keys = Set.of(folder + "中华人民共和国公司法_20181026.docx");
        assertThat(adapter.matchFileKey(m, folder, keys)).isNull();
    }

    @Test
    void matchFileKeyHandlesMissingPublishDate() {
        MappedLaw m = adapter.map(record("某规定", "某机关", null, null, 3, "地方性法规"));
        assertThat(m.versionKey()).isEqualTo("0");
        String folder = "fglaw20260527/";
        Set<String> keys = Set.of(folder + "某规定_.docx");
        assertThat(adapter.matchFileKey(m, folder, keys)).isEqualTo(folder + "某规定_.docx");
    }
}
