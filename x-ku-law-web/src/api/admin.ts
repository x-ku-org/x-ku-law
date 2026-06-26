import { http, unwrap } from './http';
import type { PageResult } from '@/types/api';
import type {
  BasicQuery,
  DictDataRecord,
  DictTypeRecord,
  FileObject,
  FilePresignPayload,
  FilePresignResult,
  LawUploadIngestPayload,
  NotificationRecord,
  PermissionRecord,
  RoleRecord,
  UserRecord
} from '@/types/admin';

export function pageUsers(params: BasicQuery) {
  return unwrap<PageResult<UserRecord>>(http.get('/system/users', { params }));
}

export function createUser(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/system/users', payload));
}

export function updateUser(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/system/users/${id}`, payload));
}

export function deleteUser(id: number | string) {
  return unwrap<void>(http.delete(`/system/users/${id}`));
}

/** 用户已分配的角色 ID 列表。 */
export function getUserRoleIds(id: number | string) {
  return unwrap<number[]>(http.get(`/system/users/${id}/roles`));
}

/** 覆盖式为用户分配角色。 */
export function assignUserRoles(id: number | string, roleIds: number[]) {
  return unwrap<void>(http.put(`/system/users/${id}/roles`, { roleIds }));
}

export function pageRoles(params: BasicQuery) {
  return unwrap<PageResult<RoleRecord>>(http.get('/system/roles', { params }));
}

export function createRole(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/system/roles', payload));
}

export function updateRole(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/system/roles/${id}`, payload));
}

export function deleteRole(id: number | string) {
  return unwrap<void>(http.delete(`/system/roles/${id}`));
}

/** 角色已分配的权限 ID 列表。 */
export function getRolePermissionIds(id: number | string) {
  return unwrap<number[]>(http.get(`/system/roles/${id}/permissions`));
}

/** 覆盖式为角色分配权限。 */
export function assignRolePermissions(id: number | string, permissionIds: number[]) {
  return unwrap<void>(http.put(`/system/roles/${id}/permissions`, { permissionIds }));
}

export function pagePermissions(params: BasicQuery) {
  return unwrap<PageResult<PermissionRecord>>(http.get('/system/permissions', { params }));
}

export function getAllPermissions() {
  return unwrap<PermissionRecord[]>(http.get('/system/permissions/all'));
}

export function createPermission(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/system/permissions', payload));
}

export function updatePermission(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/system/permissions/${id}`, payload));
}

export function deletePermission(id: number | string) {
  return unwrap<void>(http.delete(`/system/permissions/${id}`));
}

export function pageDictTypes(params: BasicQuery) {
  return unwrap<PageResult<DictTypeRecord>>(http.get('/system/dict/types', { params }));
}

export function createDictType(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/system/dict/types', payload));
}

export function updateDictType(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/system/dict/types/${id}`, payload));
}

export function deleteDictType(id: number | string) {
  return unwrap<void>(http.delete(`/system/dict/types/${id}`));
}

export function pageDictData(params: BasicQuery & { dictCode?: string }) {
  return unwrap<PageResult<DictDataRecord>>(http.get('/system/dict/data', { params }));
}

export function createDictData(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/system/dict/data', payload));
}

export function updateDictData(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/system/dict/data/${id}`, payload));
}

export function deleteDictData(id: number | string) {
  return unwrap<void>(http.delete(`/system/dict/data/${id}`));
}

export function listDictData(dictCode: string) {
  return unwrap<DictDataRecord[]>(http.get('/system/dict/data/list', { params: { dictCode } }));
}

/** 批量按字典编码取启用数据，返回 dictCode → 数据列表。前端启动时一次性加载核心词表。 */
export function batchDictData(codes: string[]) {
  return unwrap<Record<string, DictDataRecord[]>>(
    http.get('/system/dict/data/batch', { params: { codes: codes.join(',') } })
  );
}

export function pageNotifications(params: BasicQuery) {
  return unwrap<PageResult<NotificationRecord>>(http.get('/system/notifications', { params }));
}

export function createNotification(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/system/notifications', payload));
}

export function deleteNotification(id: number | string) {
  return unwrap<void>(http.delete(`/system/notifications/${id}`));
}

export function presignFile(payload: FilePresignPayload) {
  return unwrap<FilePresignResult>(http.post('/files/presign', payload));
}

export function completeFile(id: number | string) {
  return unwrap<FileObject>(http.post(`/files/${id}/complete`));
}

export function getFileMeta(id: number | string) {
  return unwrap<FileObject>(http.get(`/files/${id}`));
}

/** 获取文件预签名访问地址（下载/预览用）。 */
export function getFileUrl(id: number | string) {
  return unwrap<string>(http.get(`/files/${id}/url`));
}

export function ingestUploadedLaw(payload: LawUploadIngestPayload) {
  return unwrap<number>(http.post('/law/ingest/upload', payload));
}

export interface CollectScanParams {
  sourceCode?: string;
  batchSize?: number;
}

export function scanCollectIngest(params: CollectScanParams = {}) {
  return unwrap<Record<string, unknown>>(http.post('/collect/ingest/scan', null, { params }));
}

export function recoverStuckCollectIngest() {
  return unwrap<number>(http.post('/collect/ingest/recover-stuck'));
}
