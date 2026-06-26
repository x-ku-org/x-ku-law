import type { RouteLocationRaw } from 'vue-router';

/** 命名路由跳转帮助函数：集中维护，避免在各视图里手拼路径字符串。 */

export function lawDetailTo(documentId: string | number): RouteLocationRaw {
  return { name: 'law.detail', params: { documentId: String(documentId) } };
}

export function lawCompareTo(documentId: string | number): RouteLocationRaw {
  return { name: 'law.compare', params: { documentId: String(documentId) } };
}

export function lawSearchTo(keyword?: string): RouteLocationRaw {
  return { name: 'law.search', query: keyword ? { keyword } : undefined };
}
