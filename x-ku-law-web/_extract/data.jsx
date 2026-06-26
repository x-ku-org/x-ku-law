// === src/data.jsx ===
(function(){
// Mock data for X-KU 法规智询

const MOCK_LAW = {
  id: "L-2021-0610-001",
  title: "中华人民共和国数据安全法",
  titleEn: "Data Security Law of the People's Republic of China",
  docNo: "中华人民共和国主席令 第八十四号",
  issuingBody: "全国人民代表大会常务委员会",
  level: "法律",
  region: "全国",
  category: ["网络与数据", "国家安全"],
  publishedAt: "2021-06-10",
  effectiveAt: "2021-09-01",
  status: "现行有效",
  versions: 3,
  articlesTotal: 55,
  citedBy: 312,
  relatedRegulations: 47,
  changeLogSummary: "2024-07 修订，新增数据出境安全管理 12 条；强化重要数据目录管理。",
};

const ARTICLES = [
  {
    no: "第一条",
    cn: "为了规范数据处理活动，保障数据安全，促进数据开发利用，保护个人、组织的合法权益，维护国家主权、安全和发展利益，制定本法。",
    chapter: "第一章 总则",
    tags: ["立法目的"],
  },
  {
    no: "第二条",
    cn: "在中华人民共和国境内开展数据处理活动及其安全监管，适用本法。在中华人民共和国境外开展数据处理活动，损害中华人民共和国国家安全、公共利益或者公民、组织合法权益的，依法追究法律责任。",
    chapter: "第一章 总则",
    tags: ["适用范围", "域外效力"],
    obligation: false,
  },
  {
    no: "第二十一条",
    cn: "国家建立数据分类分级保护制度，根据数据在经济社会发展中的重要程度，以及一旦遭到篡改、破坏、泄露或者非法获取、非法利用，对国家安全、公共利益或者个人、组织合法权益造成的危害程度，对数据实行分类分级保护。",
    chapter: "第三章 数据安全制度",
    tags: ["分类分级", "核心义务"],
    obligation: true,
    annotation: "依据本条，已于 2024-03 发布《重要数据目录管理办法（试行）》。",
  },
  {
    no: "第二十七条",
    cn: "开展数据处理活动应当依照法律、法规的规定，建立健全全流程数据安全管理制度，组织开展数据安全教育培训，采取相应的技术措施和其他必要措施，保障数据安全。利用互联网等信息网络开展数据处理活动，应当在网络安全等级保护制度的基础上，履行上述数据安全保护义务。",
    chapter: "第四章 数据安全保护义务",
    tags: ["义务", "等保"],
    obligation: true,
    citations: 47,
  },
  {
    no: "第三十条",
    cn: "重要数据的处理者应当按照规定对其数据处理活动定期开展风险评估，并向有关主管部门报送风险评估报告。",
    chapter: "第四章 数据安全保护义务",
    tags: ["风险评估", "报告义务"],
    obligation: true,
    citations: 89,
    annotation: "频次：每年至少一次；模板见运营 2025-01 发布的《合规清单 · 数据安全 v3》。",
  },
  {
    no: "第三十一条",
    cn: "关键信息基础设施的运营者在中华人民共和国境内运营中收集和产生的重要数据的出境安全管理，适用《中华人民共和国网络安全法》的规定；其他数据处理者在中华人民共和国境内运营中收集和产生的重要数据的出境安全管理办法，由国家网信部门会同国务院有关部门制定。",
    chapter: "第四章 数据安全保护义务",
    tags: ["数据出境", "关键信息基础设施"],
    obligation: true,
  },
];

const RECENT_ALERTS = [
  {
    id: "AL-9012",
    severity: "high",
    title: "《数据安全法》司法解释（二）于本周公开征求意见",
    when: "今早 09:12",
    matched: 3,
    body: "命中订阅规则「数据合规 · 高优」，影响主体 2 个、清单事项 7 项。",
  },
  {
    id: "AL-9011",
    severity: "mid",
    title: "《个人信息出境标准合同办法》执行细则更新",
    when: "昨日 16:30",
    matched: 1,
    body: "新增 4 条具体备案要求，原版本相关清单需在 7 日内复核。",
  },
  {
    id: "AL-9008",
    severity: "low",
    title: "上海市生成式AI服务管理细则 v2 公开征求意见",
    when: "5 月 21 日",
    matched: 1,
    body: "地方层级；与 1 个主体的产品线相关。",
  },
];

const TODAY_HIGHLIGHTS = [
  { kind: "MOTION",   k: "立法动向", n: 7,  delta: "+2" },
  { kind: "REVISION", k: "重要修订", n: 3,  delta: "+1" },
  { kind: "INTERP.",  k: "新司法解释", n: 1,  delta: "" },
  { kind: "INDEX",    k: "热点话题指数", n: "82", delta: "+6%" },
];

const TIMELINE = [
  { date: "2021-06-10", label: "公布", v: "v1", isCurrent: false },
  { date: "2021-09-01", label: "施行", v: "v1", isCurrent: false },
  { date: "2024-07-15", label: "修订", v: "v2", isCurrent: false, note: "新增数据出境安全 12 条" },
  { date: "2024-09-01", label: "现行", v: "v2", isCurrent: true },
  { date: "2026-06-?",  label: "草案", v: "v3", future: true, note: "司法解释 (二) 征求意见" },
];

const SEARCH_RESULTS = [
  {
    id: "L-2021-0610-001",
    title: "中华人民共和国数据安全法",
    docNo: "主席令 第八十四号",
    level: "法律",
    region: "全国",
    body: "国家建立数据分类分级保护制度…重要数据的处理者应当对数据处理活动定期开展风险评估…",
    effective: "2021-09-01",
    status: "现行有效",
    cited: 312,
    hits: 7,
  },
  {
    id: "L-2021-1101-001",
    title: "中华人民共和国个人信息保护法",
    docNo: "主席令 第九十一号",
    level: "法律",
    region: "全国",
    body: "处理个人信息应当遵循合法、正当、必要和诚信原则…个人信息出境应当符合下列条件之一…",
    effective: "2021-11-01",
    status: "现行有效",
    cited: 521,
    hits: 12,
  },
  {
    id: "R-2023-CAC-0023",
    title: "个人信息出境标准合同办法",
    docNo: "国家网信办令 第 13 号",
    level: "部门规章",
    region: "全国",
    body: "在中华人民共和国境内运营中收集和产生的个人信息向境外提供…应当签订标准合同…",
    effective: "2023-06-01",
    status: "已修订",
    cited: 92,
    hits: 5,
    superseded: true,
  },
  {
    id: "R-2024-CAC-0044",
    title: "数据出境安全评估办法",
    docNo: "国家网信办令 第 11 号",
    level: "部门规章",
    region: "全国",
    body: "数据处理者向境外提供重要数据，应当通过所在地省级网信部门向国家网信部门申报…",
    effective: "2024-03-22",
    status: "现行有效",
    cited: 67,
    hits: 9,
  },
  {
    id: "LOC-2024-SH-0008",
    title: "上海市数据条例",
    docNo: "上海市人民代表大会 第 84 号公告",
    level: "地方性法规",
    region: "上海市",
    body: "推进公共数据开放、流通和应用…明确数据要素市场培育的政府职责…",
    effective: "2022-01-01",
    status: "现行有效",
    cited: 41,
    hits: 4,
  },
];

const AI_MESSAGES = [
  {
    role: "user",
    text: "我们公司是一家做跨境电商的，每天会有大量境内用户的订单数据需要传到香港的服务器。在 2026 年新法下，我们需要做哪些合规动作？请按优先级列出。",
    when: "10:42",
  },
  {
    role: "assistant",
    streaming: false,
    when: "10:42",
    blocks: [
      {
        kind: "tldr",
        text: "你需要完成三项强制动作：(1) 区分一般数据 / 个人信息 / 重要数据；(2) 按数据类型走对应的出境路径——标准合同备案 或 安全评估；(3) 建立年度风险评估机制。下方按优先级展开。",
      },
      {
        kind: "step",
        n: 1,
        title: "对出境数据做分类分级",
        body: "依照《数据安全法》第二十一条，国家建立数据分类分级保护制度。订单数据中通常同时包含 ① 个人信息、② 可能构成「重要数据」的聚合数据。",
        citeIds: ["DSL-21"],
      },
      {
        kind: "step",
        n: 2,
        title: "走对应的出境路径",
        body: "个人信息向境外提供，需在「安全评估、标准合同、认证」三条路径中选其一；其中标准合同适用于非关基、处理量不达申报阈值的处理者。",
        citeIds: ["PIPL-38", "SC-2023-13"],
      },
      {
        kind: "step",
        n: 3,
        title: "落地数据出境安全评估（如属重要数据）",
        body: "重要数据出境必须申报。需在所在地省级网信部门提交安全评估申请，审查周期一般为 45 个工作日。",
        citeIds: ["SA-2024-11", "DSL-31"],
      },
      {
        kind: "step",
        n: 4,
        title: "建立全流程数据安全管理与年度风险评估",
        body: "依照《数据安全法》第二十七、三十条，处理者必须建立全流程数据安全管理制度，并对处理活动定期开展风险评估、向主管部门报送报告。",
        citeIds: ["DSL-27", "DSL-30"],
      },
    ],
    citations: [
      { id: "DSL-21",  source: "中华人民共和国数据安全法", article: "第二十一条", excerpt: "国家建立数据分类分级保护制度，根据数据在经济社会发展中的重要程度…对数据实行分类分级保护。", confidence: 0.97 },
      { id: "PIPL-38", source: "中华人民共和国个人信息保护法", article: "第三十八条", excerpt: "个人信息处理者因业务等需要，确需向中华人民共和国境外提供个人信息的…", confidence: 0.94 },
      { id: "SC-2023-13", source: "个人信息出境标准合同办法", article: "第四条", excerpt: "个人信息处理者通过订立标准合同的方式向境外提供个人信息的…", confidence: 0.91 },
      { id: "SA-2024-11", source: "数据出境安全评估办法", article: "第七条", excerpt: "数据处理者申报数据出境安全评估的，应当通过所在地省级网信部门…", confidence: 0.93 },
      { id: "DSL-27", source: "中华人民共和国数据安全法", article: "第二十七条", excerpt: "开展数据处理活动应当依照法律、法规的规定，建立健全全流程数据安全管理制度…", confidence: 0.95 },
      { id: "DSL-30", source: "中华人民共和国数据安全法", article: "第三十条", excerpt: "重要数据的处理者应当按照规定对其数据处理活动定期开展风险评估，并向有关主管部门报送风险评估报告。", confidence: 0.96 },
    ],
  },
];

const COMPLIANCE_SUBJECTS = [
  { name: "杭州数擎科技 · 主体A",         score: 84, tasks: { open: 12, due: 3, done: 41 }, risk: "low" },
  { name: "境外仓 · 香港业务线",           score: 62, tasks: { open: 7,  due: 4, done: 18 }, risk: "high" },
  { name: "App「数擎洞察」",               score: 76, tasks: { open: 9,  due: 1, done: 27 }, risk: "mid" },
  { name: "B2B SaaS 平台 · 国内",          score: 91, tasks: { open: 3,  due: 0, done: 52 }, risk: "low" },
];

const COMPLIANCE_TASKS = [
  { id: "T-2026-0512-019", subject: "境外仓 · 香港业务线", title: "完成 2026 上半年数据出境安全评估申报材料", due: "2026-06-15", priority: "high",   owner: "李书航", status: "进行中", progress: 0.4, basis: "数据出境安全评估办法 第七条" },
  { id: "T-2026-0501-008", subject: "杭州数擎科技 · 主体A", title: "更新《数据分类分级目录》并报备",          due: "2026-06-30", priority: "high",   owner: "张雯",   status: "进行中", progress: 0.7, basis: "数据安全法 第二十一条" },
  { id: "T-2026-0419-003", subject: "App「数擎洞察」",     title: "个人信息出境标准合同 v2 续签",            due: "2026-06-08", priority: "mid",    owner: "王予",   status: "待审核", progress: 0.9, basis: "PI 出境标准合同办法 第四条" },
  { id: "T-2026-0408-021", subject: "境外仓 · 香港业务线", title: "年度数据安全教育培训记录归档",            due: "2026-07-01", priority: "low",    owner: "黄盛",   status: "进行中", progress: 0.2, basis: "数据安全法 第二十七条" },
];

const TASK_TIMELINE = [
  { ts: "05-12 09:20", by: "系统", what: "依据《数据出境安全评估办法 · 第七条》自动生成任务，归属「境外仓 · 香港业务线」。" },
  { ts: "05-12 14:08", by: "李书航", what: "认领任务，预估工作量 16h，目标完成日期 06-15。" },
  { ts: "05-18 11:45", by: "李书航", what: "上传《数据资产盘点 v4.xlsx》、《出境数据流图 v2.pdf》。" },
  { ts: "05-19 10:02", by: "AI 复核", what: "对比 v3 模板，标注 3 处缺失字段：① 重要数据定级依据；② 接收方安全能力；③ 安全风险及应对措施。" },
  { ts: "05-21 16:30", by: "张雯", what: "评论：第②项请补充香港接收方的 ISO 27001 证书复印件。" },
];

const VERSION_DIFF = [
  {
    no: "第二十一条",
    title: "数据分类分级保护制度",
    left:  { v: "v1 · 2021-09-01", text: "国家建立数据分类分级保护制度，根据数据在经济社会发展中的重要程度，以及一旦遭到篡改、破坏、泄露或者非法获取、非法利用，对国家安全、公共利益或者个人、组织合法权益造成的危害程度，对数据实行分类分级保护。" },
    right: { v: "v2 · 2024-09-01", text: "国家建立数据分类分级保护制度，根据数据在经济社会发展中的重要程度，以及一旦遭到篡改、破坏、泄露或者非法获取、非法利用，对国家安全、公共利益或者个人、组织合法权益造成的危害程度，对数据实行分类分级保护。关系国家安全、国民经济命脉、重要民生、重大公共利益等的数据属于核心数据，实行更加严格的管理制度。" },
    change: "modified",
    note: "新增「核心数据」定义及其更严格管理要求。",
  },
  {
    no: "第三十一条",
    title: "重要数据出境管理",
    left:  { v: "v1 · 2021-09-01", text: "关键信息基础设施的运营者在中华人民共和国境内运营中收集和产生的重要数据的出境安全管理，适用《中华人民共和国网络安全法》的规定。" },
    right: { v: "v2 · 2024-09-01", text: "关键信息基础设施的运营者在中华人民共和国境内运营中收集和产生的重要数据的出境安全管理，适用《中华人民共和国网络安全法》的规定；其他数据处理者在中华人民共和国境内运营中收集和产生的重要数据的出境安全管理办法，由国家网信部门会同国务院有关部门制定。" },
    change: "modified",
    note: "扩充非关基数据处理者的出境管理路径。",
  },
  {
    no: "第四十三条",
    title: "（新增）数据流通促进",
    left:  { v: "v1 · 2021-09-01", text: "—— 本版本未规定 ——" },
    right: { v: "v2 · 2024-09-01", text: "国家支持数据依法有序自由流动，建立数据要素市场培育机制，鼓励数据交易、数据资产入表等创新实践，强化数据流通中的安全保障。" },
    change: "added",
    note: "首次将数据流通与要素市场写入正文。",
  },
];

window.XKU_DATA = {
  MOCK_LAW, ARTICLES, RECENT_ALERTS, TODAY_HIGHLIGHTS, TIMELINE,
  SEARCH_RESULTS, AI_MESSAGES, COMPLIANCE_SUBJECTS, COMPLIANCE_TASKS,
  TASK_TIMELINE, VERSION_DIFF,
};

})();

