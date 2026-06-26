package cn.xku.law.collect.source;

import java.time.LocalDate;

/**
 * 归一化后的法规字段集合——各数据源 {@link SourceAdapter} 把原始元数据行映射成本结构，
 * 供编排器（lr-server）落库为法规主表/版本。与具体来源无关。
 *
 * @param lawUid       文档级稳定唯一标识（同一部法律的所有版本相同）。决定归并到哪个 lr_law_document。
 * @param versionKey   版本判别键（同一 lawUid 下唯一），用作 lr_law_version.version_no。如公布日 yyyyMMdd。
 * @param title        标题
 * @param docNo        文号（可空；FLK 无文号）
 * @param issuingOrg   制定/发布机关
 * @param publishDate  公布日期
 * @param effectiveDate 施行日期
 * @param status       时效状态：effective/amended/not_effective/expired/repealed/unknown
 * @param lawType      法规类型：law/regulation/standard…
 * @param legalLevel   效力级别（如 地方性法规/行政法规/national_standard）
 * @param sourceUrl    来源链接（可空）
 * @param regionCode   适用地区（省级行政区名，可空；中央/全国性法规与标准为 null）
 * @param subjectDomain 主题领域/分类名（FLK=法规分类如「行政法规」「司法解释」；GB=标准类型如「推荐性国家标准」；可空）
 * @param industryCode  行业分类编码（GB=ICS 国际标准分类号，需富集后才有；FLK 暂无；可空）
 */
public record MappedLaw(
        String lawUid,
        String versionKey,
        String title,
        String docNo,
        String issuingOrg,
        LocalDate publishDate,
        LocalDate effectiveDate,
        String status,
        String lawType,
        String legalLevel,
        String sourceUrl,
        String regionCode,
        String subjectDomain,
        String industryCode) {
}
