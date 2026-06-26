package cn.xku.law.law.domain;

import cn.xku.law.common.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/** 法规文件主表实体，对应 lr_law_document */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("lr_law_document")
public class LawDocumentDO extends BaseDO {

    /** 法规稳定唯一标识（全局不变，跨版本引用） */
    private String lawUid;

    /** 法规标题 */
    private String title;

    /** 标题拼音（全文检索辅助字段） */
    private String titlePinyin;

    /** 文号，如"国务院令第XXX号" */
    private String documentNo;

    /** 法规类型：law/regulation/rule/normative/standard/policy */
    private String lawType;

    /** 效力级别：宪法/法律/行政法规/部门规章/地方性法规/地方政府规章 等 */
    private String legalLevel;

    /** 发布机构 */
    private String issuingOrg;

    /** 适用地区行政区划代码 */
    private String regionCode;

    /** 行业编码（国民经济行业分类） */
    private String industryCode;

    /** 主题领域 */
    private String subjectDomain;

    /** 时效状态：effective/amended/not_effective/expired/repealed/unknown（amended=已修改，被新版取代的历史版本） */
    private String status;

    /** 发布日期 */
    private LocalDate publishDate;

    /** 生效日期 */
    private LocalDate effectiveDate;

    /** 失效日期 */
    private LocalDate expireDate;

    /** 版本时效：current（最新有效版本）/ history（历史版本） */
    private String timelinessStatus;

    /** 来源内容渠道 ID（关联 lr_content_source.id） */
    private Long sourceId;

    /** 官方发布链接 */
    private String officialUrl;

    /** 当前有效版本 ID（关联 lr_law_version.id） */
    private Long currentVersionId;

    /** 法规摘要 */
    private String summary;

    /** 备注 */
    private String remark;
}
