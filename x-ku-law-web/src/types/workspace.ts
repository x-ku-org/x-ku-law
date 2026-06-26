import type { PageQuery } from './api';

export interface NotificationInbox {
  /** lr_notification_receiver.id（收件记录主键） */
  receiverId: number;
  notificationId?: number;
  notificationType?: string;
  title?: string;
  content?: string;
  /** 关联业务类型，如 law_document */
  refType?: string;
  refId?: number;
  readStatus?: string;
  readTime?: string;
  sendTime?: string;
  createTime?: string;
}

export interface SubscriptionMatch {
  id: number;
  ruleId?: number;
  documentId?: number;
  versionId?: number;
  articleId?: number;
  /** 命中法规标题（命中时落库的标题快照） */
  documentTitle?: string;
  matchType?: string;
  matchReason?: string;
  matchScore?: number;
  matchedTime?: string;
  readStatus?: string;
  createTime?: string;
}

export interface SubscriptionMatchQuery extends PageQuery {
  ruleId?: number;
  readStatus?: string;
}

export interface AiSession {
  id: number;
  title?: string;
  status?: string;
  createTime?: string;
  updateTime?: string;
}

export interface AiMessageCitation {
  id: string;
  source?: string;
  article?: string;
  excerpt?: string;
  confidence?: number;
  documentId?: number;
  /** 时效：current（现行有效）/ superseded（历史版本）/ repealed（已废止/失效） */
  validityStatus?: string;
}

/** 证据账册按「回答」分组：每轮回答的 [n] 各自从 1 开始，故须按 messageId 隔离。 */
export interface AiCitationGroup {
  messageId: number;
  label: string;
  citations: AiMessageCitation[];
}

export interface AiMessageBlock {
  kind: 'tldr' | 'step' | string;
  n?: string;
  title?: string;
  body?: string;
  citeIds?: string[];
}

export interface AiMessage {
  id: number;
  sessionId: number;
  role?: 'user' | 'assistant' | string;
  content?: string;
  blocks?: AiMessageBlock[];
  citations?: AiMessageCitation[];
  /** 风险分级：normal / warning（命中失效版本/低置信/无有效引用）。 */
  riskLevel?: string;
  /** 当前用户是否已点赞（重新进入会话时恢复「已赞」状态）。 */
  liked?: boolean;
  createTime?: string;
  /** 稳定的客户端渲染键：流式占位消息在 onDone 把 id 由临时值改为真实值时，
   *  用它作 v-for key 可避免 DOM 被销毁重建（否则整块回答会闪一下/跳动）。 */
  clientKey?: number;
}

export interface InboxQuery extends PageQuery {
  readStatus?: string;
}

export interface SavedSearch {
  id: number;
  name?: string;
  keyword?: string;
  filtersJson?: string;
  notifyEnabled?: boolean;
  status?: string;
  createTime?: string;
}

export interface SavedSearchPayload {
  name: string;
  keyword?: string;
  filtersJson?: string;
  notifyEnabled?: boolean;
  status?: string;
}

export interface SubscriptionRule {
  id: number;
  ruleName?: string;
  ruleType?: string;
  keyword?: string;
  filtersJson?: string;
  deliveryChannel?: string;
  frequency?: string;
  status?: string;
  lastRunTime?: string;
  createTime?: string;
  updateTime?: string;
}

export interface SubscriptionRuleQuery extends PageQuery {
  ruleType?: string;
  status?: string;
}

export interface SubscriptionRulePayload {
  ruleName: string;
  ruleType?: string;
  keyword?: string;
  filtersJson?: string;
  deliveryChannel?: string;
  frequency?: string;
  status?: string;
}

export interface Favorite {
  id: number;
  refType?: string;
  refId?: number;
  folderName?: string;
  titleSnapshot?: string;
  createTime?: string;
}

export interface FavoritePayload {
  refType: string;
  refId: number;
  folderName?: string;
  titleSnapshot?: string;
}

export interface FavoriteQuery extends PageQuery {
  refType?: string;
  folderName?: string;
  refId?: number;
}

export interface Feedback {
  id: number;
  feedbackType?: string;
  refType?: string;
  refId?: number;
  content?: string;
  status?: string;
  createTime?: string;
}

export interface FeedbackPayload {
  feedbackType: string;
  refType?: string;
  refId?: number;
  content: string;
}

export interface FeedbackQuery extends PageQuery {
  feedbackType?: string;
  status?: string;
}
