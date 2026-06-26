package cn.xku.law.listener;

import cn.xku.law.law.domain.LawDocumentDO;
import cn.xku.law.law.event.LawVersionPublishedEvent;
import cn.xku.law.law.mapper.LawDocumentMapper;
import cn.xku.law.subscription.domain.DocumentMatchContext;
import cn.xku.law.subscription.service.SubscriptionRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionAlertEventListener {

    private final SubscriptionRuleService subscriptionRuleService;
    private final LawDocumentMapper lawDocumentMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLawVersionPublished(LawVersionPublishedEvent event) {
        if (event.isSuppressSubscriptionAlert()) {
            return; // 初始批量导入：跳过订阅预警，避免对全量历史数据触发海量预警
        }
        try {
            // 法规主数据表为公共表（tenant_id=0），异步无安全上下文亦可读取
            LawDocumentDO doc = lawDocumentMapper.selectById(event.getDocumentId());
            DocumentMatchContext ctx = new DocumentMatchContext();
            ctx.setDocumentId(event.getDocumentId());
            ctx.setVersionId(event.getVersionId());
            ctx.setMatchType(event.getMatchType());
            if (doc != null) {
                ctx.setTitle(doc.getTitle());
                ctx.setSummary(doc.getSummary());
                ctx.setLegalLevel(doc.getLegalLevel());
                ctx.setRegionCode(doc.getRegionCode());
                ctx.setIssuingOrg(doc.getIssuingOrg());
                ctx.setStatus(doc.getStatus());
            }
            subscriptionRuleService.triggerMatchForDocument(ctx);
        } catch (Exception e) {
            log.error("[SubscriptionAlertEventListener] 订阅预警触发失败: documentId={}, versionId={}",
                    event.getDocumentId(), event.getVersionId(), e);
        }
    }
}
