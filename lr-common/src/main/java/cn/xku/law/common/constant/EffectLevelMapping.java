package cn.xku.law.common.constant;

import java.util.List;
import java.util.Map;

/**
 * 效力层级 code 与中文原值的映射。
 *
 * <p>前端「效力层级」chip 传的是 code（law/regulation/...，见 web/utils/labels.ts），
 * 但库里（lr_law_document.legal_level / ES effectLevel）存的是采集时的中文原值：
 * FLK 的 flxz（法律/行政法规/地方性法规…）、GB 的标准类型（强制性国家标准/推荐性国家标准/国家标准化指导性技术文件）。
 * 直接用 code 做精确匹配永不命中，故须先把 code 展开为对应的中文原值集合。
 *
 * <p>UI 只有 5 个 chip，故把更细的层级就近归并到最接近的桶（如地方性法规→regulation、地方政府规章→rule）。
 */
public final class EffectLevelMapping {

    private EffectLevelMapping() {}

    private static final Map<String, List<String>> CODE_TO_RAW = Map.of(
            "law",        List.of("宪法", "法律", "法律解释", "司法解释"),
            "regulation", List.of("行政法规", "地方性法规", "监察法规", "经济特区法规", "自治条例", "单行条例"),
            "rule",       List.of("部门规章", "地方政府规章"),
            "normative",  List.of("规范性文件"),
            "standard",   List.of("强制性国家标准", "推荐性国家标准", "国家标准化指导性技术文件", "国家标准")
    );

    /**
     * 把效力层级 code 展开为对应的中文原值集合；非已知 code（已是中文原值或自定义）原样返回单元素列表，
     * 保证向后兼容与精确匹配仍可用。
     */
    public static List<String> toRawValues(String codeOrRaw) {
        if (codeOrRaw == null) {
            return List.of();
        }
        List<String> raw = CODE_TO_RAW.get(codeOrRaw);
        return raw != null ? raw : List.of(codeOrRaw);
    }
}
