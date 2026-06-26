import { http, unwrap } from './http';
import type { PageResult } from '@/types/api';
import type { LawArticle, LawDocument, LawRelation, LawSearchPage, LawSearchQuery, LawVersion, VersionDiffResult } from '@/types/law';

export function searchLaws(params: LawSearchQuery) {
  return unwrap<LawSearchPage>(http.get('/search/laws', { params }));
}

export function getLawDocument(id: number | string) {
  return unwrap<LawDocument>(http.get(`/law/documents/${id}`));
}

export function getLawVersions(documentId: number | string) {
  return unwrap<PageResult<LawVersion>>(http.get('/law/versions', { params: { documentId, pageNo: 1, pageSize: 100 } }));
}

export function getLawArticles(documentId: number | string, versionId?: number | string) {
  return unwrap<PageResult<LawArticle>>(http.get('/law/articles', { params: { documentId, versionId, pageNo: 1, pageSize: 100 } }));
}

export function getLawRelations(sourceDocumentId: number | string) {
  return unwrap<PageResult<LawRelation>>(http.get('/law/relations', { params: { sourceDocumentId, pageNo: 1, pageSize: 20 } }));
}

export function compareLawVersions(baseVersionId: number | string, targetVersionId: number | string) {
  return unwrap<VersionDiffResult>(http.get('/law/compare', { params: { baseVersionId, targetVersionId } }));
}

export interface LawInterpretation {
  id: number;
  documentId: number;
  versionId: number;
  model?: string;
  interpretationText?: string;
  status?: string;
  updateTime?: string;
}

/** 取法规解读：传 versionId 取指定版本，否则取 documentId 的现行版。未生成时后端返回 null。 */
export function getLawInterpretation(params: { documentId?: number | string; versionId?: number | string }) {
  return unwrap<LawInterpretation | null>(http.get('/law/interpretation', { params }));
}

export function pageLawDocuments(params: Record<string, unknown>) {
  return unwrap<PageResult<LawDocument>>(http.get('/law/documents', { params }));
}

export function createLawDocument(payload: Partial<LawDocument> & { lawUid: string; title: string; lawType: string; status: string }) {
  return unwrap<number>(http.post('/law/documents', payload));
}

export function updateLawDocument(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/law/documents/${id}`, payload));
}

export function deleteLawDocument(id: number | string) {
  return unwrap<void>(http.delete(`/law/documents/${id}`));
}

export function pageLawVersions(params: Record<string, unknown>) {
  return unwrap<PageResult<LawVersion>>(http.get('/law/versions', { params }));
}

export function createLawVersion(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/law/versions', payload));
}

export function updateLawVersion(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/law/versions/${id}`, payload));
}

export function deleteLawVersion(id: number | string) {
  return unwrap<void>(http.delete(`/law/versions/${id}`));
}

export function publishLawVersion(id: number | string) {
  return unwrap<void>(http.put(`/law/versions/${id}/publish`));
}

export function pageLawArticles(params: Record<string, unknown>) {
  return unwrap<PageResult<LawArticle>>(http.get('/law/articles', { params }));
}

export function createLawArticle(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/law/articles', payload));
}

export function updateLawArticle(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/law/articles/${id}`, payload));
}

export function deleteLawArticle(id: number | string) {
  return unwrap<void>(http.delete(`/law/articles/${id}`));
}

export function pageLawRelations(params: Record<string, unknown>) {
  return unwrap<PageResult<LawRelation>>(http.get('/law/relations', { params }));
}

export function createLawRelation(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/law/relations', payload));
}

export function updateLawRelation(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/law/relations/${id}`, payload));
}

export function deleteLawRelation(id: number | string) {
  return unwrap<void>(http.delete(`/law/relations/${id}`));
}

export function pageLawCategories(params: Record<string, unknown>) {
  return unwrap<PageResult<Record<string, unknown>>>(http.get('/law/categories', { params }));
}

export function createLawCategory(payload: Record<string, unknown>) {
  return unwrap<number>(http.post('/law/categories', payload));
}

export function updateLawCategory(id: number | string, payload: Record<string, unknown>) {
  return unwrap<void>(http.put(`/law/categories/${id}`, payload));
}

export function deleteLawCategory(id: number | string) {
  return unwrap<void>(http.delete(`/law/categories/${id}`));
}

export function listLawCategories() {
  return unwrap<Record<string, unknown>[]>(http.get('/law/categories/all'));
}
