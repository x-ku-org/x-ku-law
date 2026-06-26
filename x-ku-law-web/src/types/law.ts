import type { PageQuery } from './api';

export interface LawSearchQuery extends PageQuery {
  keyword?: string;
  effectLevel?: string;
  status?: string;
  publishAuthority?: string;
  /** 适用地区（省级行政区中文名，与后端 region_code 索引值一致） */
  regionCode?: string;
  /** 排序方式：relevance（默认）/ time_desc / time_asc */
  sort?: string;
}

export interface LawSearchResult {
  versionId: number;
  documentId: number;
  title: string;
  docNumber?: string;
  effectLevel?: string;
  status?: string;
  publishAuthority?: string;
  effectiveDate?: string;
  highlights?: Record<string, string[]>;
}

/** 效力层级 × 年份分布桶，跨全部命中（不受分页限制） */
export interface MatrixBucket {
  effectLevel: string;
  year: number;
  count: number;
}

/** 法规检索分页结果：标准分页字段 + 跨全结果集的统计（documentCount / matrix） */
export interface LawSearchPage {
  total: number;
  list: LawSearchResult[];
  filteredCount?: number;
  /** 去重文件数（按 documentId 去重，跨全部命中） */
  documentCount: number;
  /** 效力层级 × 年份分布（跨全部命中），供前端绘制矩阵 */
  matrix: MatrixBucket[];
}

export interface LawDocument {
  id: number;
  lawUid?: string;
  title: string;
  documentNo?: string;
  lawType?: string;
  legalLevel?: string;
  issuingOrg?: string;
  regionCode?: string;
  status?: string;
  publishDate?: string;
  effectiveDate?: string;
  expireDate?: string;
  timelinessStatus?: string;
  currentVersionId?: number;
  summary?: string;
  officialUrl?: string;
  /** 文档关联标签（lr_tag，如主题词、AI 抽取关键词） */
  tags?: string[];
  createTime?: string;
  updateTime?: string;
}

export interface LawArticle {
  id: number;
  documentId: number;
  /** 冗余：所属法规标题（后端分页时回填）。 */
  documentTitle?: string;
  versionId: number;
  parentArticleId?: number;
  articleNo?: string;
  articleTitle?: string;
  chapterNo?: string;
  chapterTitle?: string;
  sectionNo?: string;
  sectionTitle?: string;
  articleOrder?: number;
  articleLevel?: number;
  contentText?: string;
  obligationFlag?: boolean;
  penaltyFlag?: boolean;
  status?: string;
}

export interface LawVersion {
  id: number;
  documentId: number;
  /** 冗余：所属法规标题，便于列表/选择器直接展示可读名（后端分页时回填）。 */
  documentTitle?: string;
  versionNo?: string;
  versionName?: string;
  revisionType?: string;
  versionStatus?: string;
  publishDate?: string;
  effectiveDate?: string;
  expireDate?: string;
  decisionDocNo?: string;
  sourceUrl?: string;
  fileId?: number;
  diffSummary?: string;
  auditStatus?: string;
}

export interface LawRelation {
  id: number;
  sourceDocumentId: number;
  targetDocumentId: number;
  relationType?: string;
  relationDesc?: string;
  relationDate?: string;
  /** 冗余：来源/目标法规标题（后端分页时回填，targetTitle 已有先例）。 */
  sourceTitle?: string;
  targetTitle?: string;
}

export type ArticleChangeType = 'ADDED' | 'REMOVED' | 'MODIFIED' | 'UNCHANGED';

export interface ArticleChange {
  articleNo?: string;
  articleTitle?: string;
  changeType: ArticleChangeType;
  baseText?: string;
  targetText?: string;
}

export interface VersionDiffResult {
  baseVersionId: number;
  targetVersionId: number;
  addedCount: number;
  removedCount: number;
  modifiedCount: number;
  unchangedCount: number;
  changeCount: number;
  summary?: string;
  changes: ArticleChange[];
}
