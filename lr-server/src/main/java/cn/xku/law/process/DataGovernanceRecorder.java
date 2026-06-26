package cn.xku.law.process;

import cn.xku.law.law.domain.DataAuditRecordDO;
import cn.xku.law.law.domain.DataQualityIssueDO;
import cn.xku.law.law.mapper.DataAuditRecordMapper;
import cn.xku.law.law.mapper.DataQualityIssueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据治理留痕（轻量）：管线在自动发布的同时记录质量问题与审核留痕，
 * 满足「来源可追溯 / 审核记录」验收，但不阻断发布。所有写入 best-effort，
 * 失败仅记日志，绝不影响主管线。对应表 lr_data_quality_issue / lr_data_audit_record（平台表）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataGovernanceRecorder {

    private final DataQualityIssueMapper qualityIssueMapper;
    private final DataAuditRecordMapper auditRecordMapper;

    /** 记录一条数据质量问题（如解析退化、缺字段、状态冲突）。 */
    public void recordQualityIssue(String refType, Long refId, String issueType,
                                   String issueLevel, String desc) {
        try {
            DataQualityIssueDO issue = new DataQualityIssueDO();
            issue.setRefType(refType);
            issue.setRefId(refId);
            issue.setIssueType(issueType);
            issue.setIssueLevel(issueLevel != null ? issueLevel : "normal");
            issue.setIssueDesc(truncate(desc, 1024));
            issue.setStatus("open");
            qualityIssueMapper.insert(issue);
        } catch (Exception e) {
            log.warn("[DataGovernance] 记录质量问题失败 refType={}, refId={}: {}", refType, refId, e.getMessage());
        }
    }

    /** 发布留痕：自动发布记为 pass（audit_user_id=0 系统自动），保留版本快照供追溯。 */
    public void recordPublishAudit(Long versionId, Long documentId, String afterJson) {
        try {
            DataAuditRecordDO record = new DataAuditRecordDO();
            record.setAuditType("version_publish");
            record.setRefType("law_version");
            record.setRefId(versionId);
            record.setAuditStatus("pass");
            record.setAuditUserId(0L); // 系统自动发布
            record.setAuditComment("管线自动发布（automatic publish）");
            record.setAfterJson(afterJson);
            record.setAuditTime(LocalDateTime.now());
            auditRecordMapper.insert(record);
        } catch (Exception e) {
            log.warn("[DataGovernance] 记录发布审核失败 versionId={}: {}", versionId, e.getMessage());
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
