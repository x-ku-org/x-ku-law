package cn.xku.law.law.domain.dto;

import cn.xku.law.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 法规文件分页查询参数 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LawDocumentQueryDTO extends PageParam {

    /** 关键词（模糊匹配 title / document_no） */
    private String keyword;

    /** 法规类型 */
    private String lawType;

    /** 效力级别 */
    private String legalLevel;

    /** 时效状态 */
    private String status;

    /** 适用地区代码 */
    private String regionCode;

    /** 发布机构（模糊匹配） */
    private String issuingOrg;
}
