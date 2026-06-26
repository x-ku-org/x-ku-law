import {
  effectLevelOptions,
  lawTypeOptions,
  regionOptions,
  relationTypeOptions,
  revisionTypeOptions,
  timelinessOptions,
  versionStatusOptions
} from '@/utils/labels';
import {
  createLawArticle,
  createLawCategory,
  createLawDocument,
  createLawRelation,
  createLawVersion,
  deleteLawArticle,
  deleteLawCategory,
  deleteLawDocument,
  deleteLawRelation,
  deleteLawVersion,
  pageLawArticles,
  pageLawCategories,
  pageLawDocuments,
  pageLawRelations,
  pageLawVersions,
  publishLawVersion,
  updateLawArticle,
  updateLawCategory,
  updateLawDocument,
  updateLawRelation,
  updateLawVersion
} from '@/api/law';
import type { OptionItem } from '@/types/api';
import type { RelationResource } from '@/utils/relationSources';

export interface FieldConfig {
  key: string;
  label: string;
  required?: boolean;
  type?: 'text' | 'textarea' | 'select' | 'date' | 'checkbox' | 'number' | 'relation' | 'json';
  options?: OptionItem[];
  /** type==='select'：选项来源字典编码；运行期优先取字典，缺失回退 options。 */
  dictCode?: string;
  placeholder?: string;
  hint?: string;
  /** 分组标题；同组字段聚在一个小节，未指定归入第一组（默认"基础信息"）。 */
  group?: string;
  /** 字段可见时机：both（默认）/ create（仅新建）/ edit（仅编辑）。 */
  mode?: 'both' | 'create' | 'edit';
  /** 编辑态只读：展示但禁止修改（身份/系统流转字段）。 */
  readonly?: boolean;
  /** type==='relation'：用搜索选择器代替手填外键 ID。 */
  relation?: {
    resource: RelationResource;
    /** 级联依赖的本表单字段（如版本依赖所属法规）。 */
    dependsOn?: string;
    /** 传给搜索接口的参数名，默认取 dependsOn。 */
    queryKey?: string;
    /** 编辑态回显用：行内冗余名字段（如 documentTitle）。 */
    labelField?: string;
  };
}

export interface ColumnConfig {
  key: string;
  label: string;
  width?: string;
  /** 特殊渲染：status→状态徽标，datetime→时间，bool→是/否，relation→可读名。 */
  type?: 'status' | 'datetime' | 'bool' | 'relation';
  /** type==='relation'：优先显示该冗余名字段，缺失回退 #id。 */
  labelField?: string;
}

/** 行级附加动作（如法规版本「发布」），按 show 决定是否对某行展示。 */
export interface ResourceAction {
  label: string;
  run: (id: string | number) => Promise<unknown>;
  show?: (row: Record<string, unknown>) => boolean;
  successText?: string;
}

export interface AssignOption {
  id: number;
  label: string;
  /** 次要说明（编码等），列表里灰字显示 */
  sub?: string;
}

/** 多对多分配（用户↔角色、角色↔权限）：行级"分配"动作，弹窗内多选保存。 */
export interface AssignConfig {
  label: string;
  title: string;
  /** 全部可选项 */
  options: () => Promise<AssignOption[]>;
  /** 该记录已分配的 id 列表 */
  loadAssigned: (id: number) => Promise<number[]>;
  /** 覆盖式保存分配 */
  save: (id: number, ids: number[]) => Promise<unknown>;
  successText?: string;
  emptyText?: string;
}

export interface ResourceConfig {
  title: string;
  kicker: string;
  description: string;
  /** table：默认表格；feed：预警/消息等编辑型列表 */
  layout?: 'table' | 'feed';
  columns: ColumnConfig[];
  /** 分组顺序；未声明则按字段出现顺序聚合。 */
  groups?: string[];
  /**
   * 列表筛选项：keyword 是否显示关键词框（默认显示）；status 为该资源专属状态项（不含"全部"）；
   * statusDict 指定状态项的字典编码，运行期优先取字典、缺失回退 status。
   */
  filters?: { keyword?: boolean; status?: OptionItem[]; statusDict?: string };
  loader: (params: Record<string, unknown>) => Promise<{ total: number; list: unknown[] }>;
  create?: (payload: any) => Promise<unknown>;
  update?: (id: string | number, payload: any) => Promise<unknown>;
  remove?: (id: string | number) => Promise<unknown>;
  markRead?: (id: string | number) => Promise<unknown>;
  extraAction?: ResourceAction;
  /** 多对多分配能力（用户分配角色 / 角色分配权限）。 */
  assign?: AssignConfig;
  fields?: FieldConfig[];
}

/** 通用启用/停用（字段选择与筛选共用） */
const enabledOptions: OptionItem[] = [
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' }
];

const categoryTypeOptions: OptionItem[] = [
  { label: '主题', value: 'subject' },
  { label: '地区', value: 'region' },
  { label: '行业', value: 'industry' },
  { label: '效力层级', value: 'legal_level' }
];
// 说明：用户态资源（订阅规则 / 预警 / 消息 / 收藏 / 保存检索 / 反馈）已各自拥有专属视图，
// 不再走通用 ResourceListView。此处仅保留管理后台的配置驱动资源。
export const resourceConfigs: Record<string, ResourceConfig> = {
  lawDocuments: {
    title: '法规主数据',
    kicker: '§ Law Master',
    description: '维护法规标题、文号、层级、地区、时效和来源。',
    groups: ['基础信息', '分类与层级', '时效与来源', '摘要备注'],
    columns: [
      { key: 'title', label: '标题' },
      { key: 'documentNo', label: '文号', width: '160px' },
      { key: 'legalLevel', label: '层级', width: '120px' },
      { key: 'issuingOrg', label: '发布机关', width: '260px' },
      { key: 'status', label: '状态', width: '120px', type: 'status' }
    ],
    filters: { keyword: true, status: timelinessOptions, statusDict: 'timeliness_status' },
    loader: pageLawDocuments,
    create: createLawDocument,
    update: updateLawDocument,
    remove: deleteLawDocument,
    fields: [
      { key: 'title', label: '标题', required: true, group: '基础信息' },
      { key: 'documentNo', label: '文号', group: '基础信息' },
      { key: 'lawType', label: '法规类型', required: true, type: 'select', dictCode: 'law_type', options: lawTypeOptions, group: '基础信息' },
      { key: 'issuingOrg', label: '发布机关', group: '基础信息' },
      {
        key: 'lawUid',
        label: '法规唯一标识',
        mode: 'edit',
        readonly: true,
        group: '基础信息',
        hint: '系统自动生成，不可修改。'
      },
      { key: 'legalLevel', label: '法律层级', type: 'select', dictCode: 'effect_level', options: effectLevelOptions, group: '分类与层级' },
      { key: 'regionCode', label: '适用地区', type: 'select', dictCode: 'region', options: regionOptions, group: '分类与层级' },
      { key: 'subjectDomain', label: '主题领域', group: '分类与层级' },
      { key: 'industryCode', label: '行业编码', group: '分类与层级', hint: '如国标 ICS 分类编码，可留空。' },
      { key: 'status', label: '时效状态', required: true, type: 'select', dictCode: 'timeliness_status', options: timelinessOptions, group: '时效与来源' },
      { key: 'publishDate', label: '发布日期', type: 'date', group: '时效与来源' },
      { key: 'effectiveDate', label: '生效日期', type: 'date', group: '时效与来源' },
      { key: 'expireDate', label: '失效日期', type: 'date', group: '时效与来源' },
      { key: 'officialUrl', label: '官方链接', group: '时效与来源' },
      { key: 'summary', label: '摘要', type: 'textarea', group: '摘要备注' },
      { key: 'remark', label: '备注', type: 'textarea', group: '摘要备注' }
    ]
  },
  lawVersions: {
    title: '法规版本',
    kicker: '§ Versions',
    description: '查看法规版本、生效时间、审核状态和发布状态。',
    groups: ['关联', '基础信息', '时效', '高级'],
    columns: [
      { key: 'documentId', label: '所属法规', type: 'relation', labelField: 'documentTitle' },
      { key: 'versionNo', label: '版本号', width: '120px' },
      { key: 'versionName', label: '版本名' },
      { key: 'versionStatus', label: '版本状态', width: '120px', type: 'status' },
      { key: 'auditStatus', label: '审核状态', width: '120px', type: 'status' }
    ],
    filters: { keyword: true, status: versionStatusOptions, statusDict: 'version_status' },
    loader: pageLawVersions,
    create: createLawVersion,
    update: updateLawVersion,
    remove: deleteLawVersion,
    extraAction: {
      label: '发布',
      run: publishLawVersion,
      show: (row) => row.versionStatus !== 'published',
      successText: '版本已发布。'
    },
    fields: [
      {
        key: 'documentId',
        label: '所属法规',
        required: true,
        type: 'relation',
        relation: { resource: 'lawDocuments', labelField: 'documentTitle' },
        group: '关联'
      },
      { key: 'versionNo', label: '版本号', group: '基础信息', hint: '留空将按发布日期自动生成。' },
      { key: 'versionName', label: '版本名', group: '基础信息' },
      { key: 'revisionType', label: '修订类型', type: 'select', dictCode: 'revision_type', options: revisionTypeOptions, group: '基础信息' },
      { key: 'versionStatus', label: '版本状态', type: 'select', dictCode: 'version_status', options: versionStatusOptions, group: '基础信息' },
      { key: 'publishDate', label: '发布日期', type: 'date', group: '时效' },
      { key: 'effectiveDate', label: '生效日期', type: 'date', group: '时效' },
      { key: 'expireDate', label: '失效日期', type: 'date', group: '时效' },
      { key: 'decisionDocNo', label: '决定文号', group: '时效' },
      { key: 'sourceUrl', label: '来源链接', group: '高级' },
      { key: 'fileId', label: '文件 ID', type: 'number', group: '高级', hint: '关联原文文件，一般由上传接入自动填充。' },
      { key: 'diffSummary', label: '变更摘要', type: 'textarea', group: '高级' },
      { key: 'auditStatus', label: '审核状态', readonly: true, mode: 'edit', group: '高级', hint: '由审核流程流转，不可手填。' }
    ]
  },
  lawArticles: {
    title: '条款管理',
    kicker: '§ Articles',
    description: '查看条款结构、正文、义务标记和责任标记。',
    groups: ['关联', '结构', '正文'],
    columns: [
      { key: 'documentId', label: '所属法规', type: 'relation', labelField: 'documentTitle' },
      { key: 'articleNo', label: '条号', width: '100px' },
      { key: 'articleTitle', label: '标题' },
      { key: 'obligationFlag', label: '义务', width: '72px', type: 'bool' },
      { key: 'penaltyFlag', label: '责任', width: '72px', type: 'bool' }
    ],
    filters: { keyword: true, status: enabledOptions },
    loader: pageLawArticles,
    create: createLawArticle,
    update: updateLawArticle,
    remove: deleteLawArticle,
    fields: [
      {
        key: 'documentId',
        label: '所属法规',
        required: true,
        type: 'relation',
        relation: { resource: 'lawDocuments', labelField: 'documentTitle' },
        group: '关联'
      },
      {
        key: 'versionId',
        label: '所属版本',
        required: true,
        type: 'relation',
        relation: { resource: 'lawVersions', dependsOn: 'documentId', queryKey: 'documentId' },
        group: '关联'
      },
      {
        key: 'parentArticleId',
        label: '父条款',
        type: 'relation',
        relation: { resource: 'lawArticles', dependsOn: 'documentId', queryKey: 'documentId' },
        group: '关联'
      },
      { key: 'chapterNo', label: '章号', group: '结构' },
      { key: 'chapterTitle', label: '章标题', group: '结构' },
      { key: 'sectionNo', label: '节号', group: '结构' },
      { key: 'sectionTitle', label: '节标题', group: '结构' },
      { key: 'articleNo', label: '条号', group: '结构' },
      { key: 'articleTitle', label: '条标题', group: '结构' },
      { key: 'articleOrder', label: '排序', type: 'number', group: '结构' },
      { key: 'articleLevel', label: '层级', type: 'number', group: '结构' },
      { key: 'contentText', label: '正文', required: true, type: 'textarea', group: '正文' },
      { key: 'obligationFlag', label: '义务标记', type: 'checkbox', placeholder: '该条款包含义务', group: '正文' },
      { key: 'penaltyFlag', label: '责任标记', type: 'checkbox', placeholder: '该条款包含责任', group: '正文' },
      { key: 'status', label: '状态', type: 'select', options: enabledOptions, group: '正文' }
    ]
  },
  lawCategories: {
    title: '法规分类',
    kicker: '§ Categories',
    description: '维护法规分类树和分类状态。',
    columns: [
      { key: 'categoryCode', label: '编码' },
      { key: 'categoryName', label: '名称' },
      { key: 'categoryType', label: '类型' },
      { key: 'sortOrder', label: '排序', width: '72px' },
      { key: 'status', label: '状态', width: '120px', type: 'status' }
    ],
    filters: { keyword: true, status: enabledOptions },
    loader: pageLawCategories,
    create: createLawCategory,
    update: updateLawCategory,
    remove: deleteLawCategory,
    fields: [
      {
        key: 'parentId',
        label: '父分类',
        type: 'relation',
        relation: { resource: 'lawCategories' },
        hint: '留空表示顶级分类。'
      },
      { key: 'categoryCode', label: '分类编码', required: true },
      { key: 'categoryName', label: '分类名称', required: true },
      { key: 'categoryType', label: '分类类型', type: 'select', dictCode: 'category_type', options: categoryTypeOptions },
      { key: 'sortOrder', label: '排序', type: 'number' },
      { key: 'status', label: '状态', type: 'select', options: enabledOptions }
    ]
  },
  lawRelations: {
    title: '法规关系',
    kicker: '§ Relations',
    description: '维护法规之间的引用、修订、废止、解释和冲突关系。',
    groups: ['关系', '关联对象', '说明'],
    columns: [
      { key: 'sourceDocumentId', label: '来源法规', type: 'relation', labelField: 'sourceTitle' },
      { key: 'targetDocumentId', label: '目标法规', type: 'relation', labelField: 'targetTitle' },
      { key: 'relationType', label: '关系', width: '100px' },
      { key: 'relationDesc', label: '说明' },
      { key: 'relationDate', label: '日期', width: '140px' }
    ],
    filters: { keyword: true },
    loader: pageLawRelations,
    create: createLawRelation,
    update: updateLawRelation,
    remove: deleteLawRelation,
    fields: [
      { key: 'relationType', label: '关系类型', required: true, type: 'select', dictCode: 'relation_type', options: relationTypeOptions, group: '关系' },
      { key: 'relationDate', label: '关系日期', type: 'date', group: '关系' },
      {
        key: 'sourceDocumentId',
        label: '来源法规',
        required: true,
        type: 'relation',
        relation: { resource: 'lawDocuments', labelField: 'sourceTitle' },
        group: '关联对象'
      },
      {
        key: 'sourceVersionId',
        label: '来源版本',
        type: 'relation',
        relation: { resource: 'lawVersions', dependsOn: 'sourceDocumentId', queryKey: 'documentId' },
        group: '关联对象'
      },
      {
        key: 'sourceArticleId',
        label: '来源条款',
        type: 'relation',
        relation: { resource: 'lawArticles', dependsOn: 'sourceDocumentId', queryKey: 'documentId' },
        group: '关联对象'
      },
      {
        key: 'targetDocumentId',
        label: '目标法规',
        required: true,
        type: 'relation',
        relation: { resource: 'lawDocuments', labelField: 'targetTitle' },
        group: '关联对象'
      },
      {
        key: 'targetVersionId',
        label: '目标版本',
        type: 'relation',
        relation: { resource: 'lawVersions', dependsOn: 'targetDocumentId', queryKey: 'documentId' },
        group: '关联对象'
      },
      {
        key: 'targetArticleId',
        label: '目标条款',
        type: 'relation',
        relation: { resource: 'lawArticles', dependsOn: 'targetDocumentId', queryKey: 'documentId' },
        group: '关联对象'
      },
      { key: 'relationDesc', label: '关系说明', type: 'textarea', group: '说明' }
    ]
  }
};
