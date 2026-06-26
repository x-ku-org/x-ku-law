/**
 * 关系选择器数据源注册表：把"外键字段 → 用哪个搜索接口、如何展示成可读名称"集中在一处。
 * XRelationSelect 与 resourceConfigs 都从这里取，避免外键手填、并复用既有 page* 接口。
 */
import {
  getLawDocument,
  pageLawArticles,
  pageLawCategories,
  pageLawDocuments,
  pageLawVersions
} from '@/api/law';
import { pageDictTypes, pagePermissions, pageRoles, pageUsers } from '@/api/admin';

export interface RelationItem {
  id: number | string;
  label: string;
  /** 次要说明（文号、版本号、编码等），列表里灰字显示 */
  sub?: string;
}

export interface RelationSource {
  /** 按关键词分页搜索（复用既有 page* loader） */
  search: (
    params: { keyword?: string; pageNo: number; pageSize: number } & Record<string, unknown>
  ) => Promise<{ total: number; list: unknown[] }>;
  /** 按 id 取单条（用于编辑态回显）；缺失时回退到行内冗余名或 #id */
  resolve?: (id: number | string) => Promise<unknown>;
  /** 原始记录 → 可读项 */
  toItem: (raw: Record<string, unknown>) => RelationItem;
}

const str = (v: unknown) => (v === null || v === undefined ? '' : String(v));

export const relationSources: Record<string, RelationSource> = {
  lawDocuments: {
    search: pageLawDocuments,
    resolve: getLawDocument,
    toItem: (r) => ({ id: r.id as number, label: str(r.title) || `#${r.id}`, sub: str(r.documentNo) })
  },
  lawVersions: {
    search: pageLawVersions,
    toItem: (r) => ({
      id: r.id as number,
      label: str(r.versionName) || str(r.versionNo) || `#${r.id}`,
      sub: str(r.versionNo)
    })
  },
  lawArticles: {
    search: pageLawArticles,
    toItem: (r) => ({
      id: r.id as number,
      label: str(r.articleTitle) || (r.articleNo ? `第${str(r.articleNo)}条` : `#${r.id}`),
      sub: str(r.articleNo)
    })
  },
  lawCategories: {
    search: pageLawCategories,
    toItem: (r) => ({ id: r.id as number, label: str(r.categoryName) || `#${r.id}`, sub: str(r.categoryCode) })
  },
  users: {
    search: pageUsers,
    toItem: (r) => ({ id: r.id as number, label: str(r.nickname) || str(r.username) || `#${r.id}`, sub: str(r.username) })
  },
  roles: {
    search: pageRoles,
    toItem: (r) => ({ id: r.id as number, label: str(r.roleName) || `#${r.id}`, sub: str(r.roleCode) })
  },
  dictTypes: {
    search: pageDictTypes,
    toItem: (r) => ({ id: r.id as number, label: str(r.dictName) || `#${r.id}`, sub: str(r.dictCode) })
  },
  permissions: {
    search: pagePermissions,
    toItem: (r) => ({ id: r.id as number, label: str(r.permissionName) || `#${r.id}`, sub: str(r.permissionCode) })
  }
};

export type RelationResource = keyof typeof relationSources;
