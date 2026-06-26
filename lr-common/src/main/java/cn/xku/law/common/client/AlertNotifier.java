package cn.xku.law.common.client;

/**
 * 订阅预警投递抽象。订阅域（lr-module-subscription）只依赖 lr-common，无法直接调用
 * 通知中心（lr-module-system）；由 lr-server 提供基于站内信的真实实现，未接入时走 NoOp。
 */
public interface AlertNotifier {

    /**
     * 站内信投递给指定用户（实现方负责按用户真实租户落库，绕过异步上下文的 tenant=0）。
     *
     * @param userId  接收用户 ID
     * @param title   通知标题
     * @param content 通知正文
     * @param refType 业务对象类型（如 subscription_match）
     * @param refId   业务对象 ID
     * @return 成功投递返回 true；未配置投递通道返回 false（调用方据此保留 pending）
     */
    boolean notifyStation(Long userId, String title, String content, String refType, Long refId);
}
