package cn.xku.law.collect.source;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GbSourceAdapterTest {

    private final GbSourceAdapter adapter = new GbSourceAdapter();

    private static Map<String, Object> record(String stdCode, String name, String status) {
        Map<String, Object> m = new HashMap<>();
        m.put("std_code", stdCode);
        m.put("name_cn", name);
        m.put("status", status);
        m.put("pub_date", "2026-01-01");
        m.put("impl_date", "2026-06-01");
        return m;
    }

    @Test
    void standardTypeByPrefix() {
        assertThat(GbSourceAdapter.standardType("GB/T 18482-2026")).isEqualTo("推荐性国家标准");
        assertThat(GbSourceAdapter.standardType("GB/Z 1234-2026")).isEqualTo("国家标准化指导性技术文件");
        assertThat(GbSourceAdapter.standardType("GB 1589-2026")).isEqualTo("强制性国家标准");
        assertThat(GbSourceAdapter.standardType(null)).isNull();
        assertThat(GbSourceAdapter.standardType("DB11/T 1-2026")).isNull();
    }

    @Test
    void mapFillsStandardTypeAndNoRegion() {
        MappedLaw m = adapter.map(record("GB/T 18482-2026", "可逆式抽水蓄能机组启动试运行规程", "PUBLISHED"));
        assertThat(m.lawUid()).isEqualTo("GB:GB/T 18482-2026");
        assertThat(m.subjectDomain()).isEqualTo("推荐性国家标准");
        assertThat(m.regionCode()).isNull();
        assertThat(m.status()).isEqualTo("effective");
        assertThat(m.lawType()).isEqualTo("standard");
        assertThat(m.industryCode()).isNull(); // 未富集时无 ICS
    }

    @Test
    void mapReadsIcsWhenEnriched() {
        Map<String, Object> r = record("GB/T 18482-2026", "可逆式抽水蓄能机组启动试运行规程", "PUBLISHED");
        r.put("ics", "27.140"); // step1b 富集后存在
        assertThat(adapter.map(r).industryCode()).isEqualTo("27.140");
    }
}
