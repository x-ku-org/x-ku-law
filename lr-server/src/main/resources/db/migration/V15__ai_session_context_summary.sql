SET NAMES utf8mb4;

-- AI 会话上下文滚动摘要：超长对话时把较旧的轮次增量压缩进 context_summary，
-- summary_upto_message_id 标记摘要已覆盖到的最后一条消息，其后的消息仍以原文回灌。
-- 既保留早期上下文（地域/行业/限定条件/已给结论），又控住每轮 prompt 的 token 体量。
ALTER TABLE lr_ai_session
    ADD COLUMN context_summary        TEXT   NULL COMMENT '此前对话的滚动摘要（增量维护）',
    ADD COLUMN summary_upto_message_id BIGINT NULL COMMENT '摘要已覆盖到的最后一条 lr_ai_message.id';
