package cn.xku.law.subscription.domain;

import lombok.Data;

/**
 * 法规发布后用于订阅规则条件匹配的法规属性快照。
 * 由 lr-server 监听器从法规主数据组装后传入订阅域（订阅域不依赖法规域）。
 */
@Data
public class DocumentMatchContext {

    private Long documentId;
    private Long versionId;
    /** new/update/repeal */
    private String matchType;

    private String title;
    private String summary;
    /** 效力级别，如 法律/行政法规/部门规章/地方性法规 */
    private String legalLevel;
    /** 适用地区行政区划代码 */
    private String regionCode;
    /** 发布机构 */
    private String issuingOrg;
    /** 时效状态：effective/amended/expired/repealed/... */
    private String status;
}
