import { http, unwrap } from './http';
import type { PageResult } from '@/types/api';

export interface OpsTaskQuery {
  status?: string;
  pageNo?: number;
  pageSize?: number;
}

export interface OpsConfig {
  key: string;
  name: string;
  enabled: boolean;
  scheduleType: string;
  schedule: string;
  maxRetry: number | null;
  configKeys: string;
  note: string;
}

export function getOpsConfig() {
  return unwrap<OpsConfig[]>(http.get('/ops/config'));
}

export interface ProcessTask {
  id: number;
  documentId: number | null;
  versionId: number;
  fileId: number | null;
  processStatus: string;
  retryCount: number;
  errorMessage: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  createTime?: string;
}

export interface CollectRecord {
  id: number;
  taskId: number | null;
  sourceId: number | null;
  requestUrl: string | null;
  httpStatus: number | null;
  rawFileId: number | null;
  contentHash: string | null;
  collectStatus: string;
  errorMessage: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  createTime?: string;
}

export interface IndexTask {
  id: number;
  refType: string | null;
  refId: number | null;
  indexName?: string | null;
  actionType: string | null;
  syncStatus: string;
  retryCount: number;
  errorMessage: string | null;
  lastSyncTime: string | null;
}

export interface VectorTask {
  id: number;
  refType: string | null;
  refId: number | null;
  actionType: string | null;
  syncStatus: string;
  vectorIndex: string | null;
  vectorId: string | null;
  retryCount: number;
  errorMessage: string | null;
  lastSyncTime: string | null;
}

// ===== 处理管线 =====
export function getProcessTasks(params: OpsTaskQuery) {
  return unwrap<PageResult<ProcessTask>>(http.get('/ops/process-tasks', { params }));
}

export function retryProcessTask(id: number | string) {
  return unwrap<boolean>(http.post(`/ops/process-tasks/${id}/retry`));
}

/** 一键重试全部失败的处理任务，返回重置条数。 */
export function retryAllProcessTasks() {
  return unwrap<number>(http.post('/ops/process-tasks/retry-all'));
}

export function triggerProcessTasks() {
  return unwrap<string>(http.post('/ops/process-tasks/run'));
}

// ===== AI 旁路（摘要/解读） =====
export interface AiTask {
  id: number;
  documentId: number | null;
  versionId: number;
  fileId: number | null;
  processStatus: string;
  retryCount: number;
  errorMessage: string | null;
  startedAt: string | null;
  finishedAt: string | null;
  createTime?: string;
}

export function getAiTasks(params: OpsTaskQuery) {
  return unwrap<PageResult<AiTask>>(http.get('/ops/ai-tasks', { params }));
}

export function retryAiTask(id: number | string) {
  return unwrap<boolean>(http.post(`/ops/ai-tasks/${id}/retry`));
}

export function triggerAiTasks() {
  return unwrap<string>(http.post('/ops/ai-tasks/run'));
}

/** 存量回填：为已发布且尚无 AI 任务/解读的版本批量入队，返回入队条数。 */
export function backfillAiTasks() {
  return unwrap<number>(http.post('/ops/ai-tasks/backfill'));
}

// ===== 采集接入 =====
export function getCollectRecords(params: OpsTaskQuery) {
  return unwrap<PageResult<CollectRecord>>(http.get('/ops/collect/records', { params }));
}

// ===== 检索索引 =====
export function getIndexTasks(params: OpsTaskQuery) {
  return unwrap<PageResult<IndexTask>>(http.get('/ops/index-tasks', { params }));
}

export function retryIndexTask(id: number | string) {
  return unwrap<boolean>(http.post(`/ops/index-tasks/${id}/retry`));
}

/** 一键重试全部失败的索引同步任务，返回重置条数。 */
export function retryAllIndexTasks() {
  return unwrap<number>(http.post('/ops/index-tasks/retry-all'));
}

export function triggerIndexTasks() {
  return unwrap<string>(http.post('/ops/index-tasks/run'));
}

/** 存量回填：为已发布且为当前版本的法规批量入队 upsert 索引任务（如新增 regionCode 后回填全库），返回入队条数。 */
export function backfillIndexTasks() {
  return unwrap<number>(http.post('/ops/index-tasks/backfill'));
}

// ===== 向量同步 =====
export function getVectorTasks(params: OpsTaskQuery) {
  return unwrap<PageResult<VectorTask>>(http.get('/ops/vector-tasks', { params }));
}

export function retryVectorTask(id: number | string) {
  return unwrap<boolean>(http.post(`/ops/vector-tasks/${id}/retry`));
}

/** 一键重试全部失败的向量同步任务，返回重置条数。 */
export function retryAllVectorTasks() {
  return unwrap<number>(http.post('/ops/vector-tasks/retry-all'));
}

export function triggerVectorTasks() {
  return unwrap<string>(http.post('/ops/vector-tasks/run'));
}

// ===== 订阅预警投递 =====
export interface AlertDelivery {
  id: number;
  ruleId: number | null;
  matchId: number | null;
  userId: number | null;
  channel: string | null;
  sendStatus: string;
  sendTime: string | null;
  failReason: string | null;
  retryCount: number;
  createTime?: string;
}

export function getAlertDeliveries(params: OpsTaskQuery) {
  return unwrap<PageResult<AlertDelivery>>(http.get('/ops/alert-deliveries', { params }));
}

export function retryAlertDelivery(id: number | string) {
  // 后端返回空体，统一以 true 表示已触发，适配 OpsTaskTable 的 retry 协议。
  return unwrap<unknown>(http.post(`/ops/alert-deliveries/${id}/retry`)).then(() => true);
}

/** 一键重投全部失败的预警投递，返回处理条数。 */
export function retryAllAlertDeliveries() {
  return unwrap<number>(http.post('/ops/alert-deliveries/retry-all'));
}

// ===== 数据治理：质量问题 / 审核留痕 =====
export interface QualityIssue {
  id: number;
  refType: string | null;
  refId: number | null;
  issueType: string | null;
  issueLevel: string | null;
  issueDesc: string | null;
  status: string;
  ownerUserId: number | null;
  resolvedTime: string | null;
  createTime?: string;
}

export interface QualityIssueQuery extends OpsTaskQuery {
  issueType?: string;
}

export function getQualityIssues(params: QualityIssueQuery) {
  return unwrap<PageResult<QualityIssue>>(http.get('/ops/quality-issues', { params }));
}

export function resolveQualityIssue(id: number | string) {
  return unwrap<unknown>(http.put(`/ops/quality-issues/${id}/resolve`)).then(() => true);
}

export interface AuditRecord {
  id: number;
  auditType: string | null;
  refType: string | null;
  refId: number | null;
  auditStatus: string;
  auditUserId: number | null;
  auditComment: string | null;
  auditTime: string | null;
  createTime?: string;
}

export interface AuditRecordQuery extends OpsTaskQuery {
  auditType?: string;
}

export function getAuditRecords(params: AuditRecordQuery) {
  return unwrap<PageResult<AuditRecord>>(http.get('/ops/audit-records', { params }));
}
