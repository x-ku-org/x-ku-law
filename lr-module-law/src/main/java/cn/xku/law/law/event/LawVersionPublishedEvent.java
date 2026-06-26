package cn.xku.law.law.event;

import org.springframework.context.ApplicationEvent;

/** 法规版本发布事件，由 LawVersionServiceImpl.publishVersion() 触发，供订阅预警监听器消费 */
public class LawVersionPublishedEvent extends ApplicationEvent {

    private final Long documentId;
    private final Long versionId;
    /** new/update/repeal */
    private final String matchType;
    /** 是否抑制订阅预警（如初始批量导入时置 true，避免对全量历史数据触发海量预警） */
    private final boolean suppressSubscriptionAlert;

    public LawVersionPublishedEvent(Object source, Long documentId, Long versionId, String matchType) {
        this(source, documentId, versionId, matchType, false);
    }

    public LawVersionPublishedEvent(Object source, Long documentId, Long versionId, String matchType,
                                    boolean suppressSubscriptionAlert) {
        super(source);
        this.documentId = documentId;
        this.versionId = versionId;
        this.matchType = matchType;
        this.suppressSubscriptionAlert = suppressSubscriptionAlert;
    }

    public Long getDocumentId() { return documentId; }
    public Long getVersionId() { return versionId; }
    public String getMatchType() { return matchType; }
    public boolean isSuppressSubscriptionAlert() { return suppressSubscriptionAlert; }
}
