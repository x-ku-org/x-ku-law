import type { PageQuery } from './api';

export interface UserRecord {
  id: number;
  username?: string;
  nickname?: string;
  mobile?: string;
  email?: string;
  status?: string;
  createTime?: string;
}

export interface RoleRecord {
  id: number;
  roleCode?: string;
  roleName?: string;
  status?: string;
  createTime?: string;
}

export interface PermissionRecord {
  id: number;
  parentId?: number | null;
  permissionCode?: string;
  permissionName?: string;
  permissionType?: string;
  path?: string;
  component?: string;
  requestMethod?: string;
  sortOrder?: number;
  visible?: boolean;
  status?: string;
}

export interface DictTypeRecord {
  id: number;
  dictCode?: string;
  dictName?: string;
  status?: string;
  remark?: string;
}

export interface DictDataRecord {
  id: number;
  dictTypeId?: number;
  dictCode?: string;
  dictLabel?: string;
  dictValue?: string;
  parentValue?: string;
  sortOrder?: number;
  status?: string;
  extJson?: string;
}

export interface NotificationRecord {
  id: number;
  title?: string;
  content?: string;
  notificationType?: string;
  sendScope?: string;
  refType?: string;
  refId?: number;
  status?: string;
  sendTime?: string;
  createTime?: string;
}

export interface FilePresignPayload {
  originalName: string;
  contentType: string;
  fileSize: number;
  refType?: string;
}

export interface FilePresignResult {
  fileId: number;
  objectKey?: string;
  uploadUrl: string;
  expireSeconds?: number;
  method?: string;
  headers?: Record<string, string>;
}

export interface FileObject {
  id: number;
  originalName?: string;
  objectKey?: string;
  contentType?: string;
  fileSize?: number;
  status?: string;
  createTime?: string;
}

export interface LawUploadIngestPayload {
  fileId: number;
  title: string;
  lawUid?: string;
  versionNo?: string;
  documentNo?: string;
  lawType?: string;
  legalLevel?: string;
  issuingOrg?: string;
  regionCode?: string;
  status?: string;
  publishDate?: string;
  effectiveDate?: string;
  sourceUrl?: string;
}

export interface BasicQuery extends PageQuery {
  keyword?: string;
  status?: string;
}
