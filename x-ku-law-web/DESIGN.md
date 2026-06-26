# X·KU 法规智询 · Design System

> 编辑型衬线 · 几何母题 · 克制留白
> Version 1.0 · 2026.05.24

---

## 0. 设计哲学

X-KU 的视觉定位不是"表单系统"，而是**一份高端的法律期刊**——配上一台可推理的引擎。所有页面在三个张力中找平衡：

1. **专业可信 vs 当代先锋** — 法律内容必须严肃，但呈现方式可以离开"政府门户 / 老 ERP"的窠臼。
2. **信息密度 vs 阅读节奏** — 法规天生密集，必须以编辑性的版面给读者留出呼吸。
3. **机器智能 vs 人类裁断** — AI 输出始终回溯到具体条款；UI 让"哪句话来自哪一条"始终可见。

具体形式上落到三条铁律：

- **节制** — 单色 + 留白 + 细线，禁绝渐变、磨砂、玻璃拟态；几乎不使用 emoji。
- **编辑性** — § 章节编号、大号意大利体衬线、版心节奏、引文/边注 (marginalia)。
- **几何身份** — 钻石与立方体是 logo 抽象的核心，反复出现在分隔符、标记、装饰位。

---

## 1. 调色板

### 1.1 Paper (背景层级)

| Token | 值 | 用途 |
|---|---|---|
| `--paper`        | `#FFFFFF` | 主背景 (页面、阅读区) |
| `--paper-2`      | `#FAFAFA` | 副背景 (侧栏、轻凹陷区) |
| `--paper-sunk`   | `#F2F2F4` | 深凹陷 (轨道、未激活按钮) |
| `--paper-card`   | `#FFFFFF` | 卡片基色 |

> **原则**：纯白为主。需要"层"的时候用极淡冷灰，不要用暖米色或大面积彩色。

### 1.2 Ink (墨色层级)

源自 logo 深海军蓝。

| Token | 值 | 用途 |
|---|---|---|
| `--ink`     | `#0B1530` | 标题、正文加粗、深色面板底色 |
| `--ink-2`   | `#2A3553` | 正文 |
| `--ink-3`   | `#4A536F` | 次要正文 |
| `--muted`   | `#71758A` | meta / 说明 |
| `--muted-2` | `#A4A7B6` | 占位、disabled |

### 1.3 Rule (分隔线)

| Token | 值 | 用途 |
|---|---|---|
| `--rule`        | `#E7E7EB` | 默认 hairline |
| `--rule-strong` | `#D4D4DA` | 强分隔、active 边框 |
| `--rule-ink`    | `rgba(11, 21, 48, 0.10)` | 版心栅格、Tweaks 视图 |

> **使用**：尽量用 1px 实线，不使用阴影做分组。`hairline-strong` 用于章节顶部和"页面 - 章节"层级的分隔。

### 1.4 Accent (强调色)

| Token | 值 | 寓意 |
|---|---|---|
| `--accent`      | `#1E5BFF` | logo 钻石中心，电光蓝 — 用于交互、引用、命中 |
| `--accent-deep` | 派生 60% darker | 深色面板上的强调文字 |
| `--accent-soft` | `rgba(30,91,255,0.08)` | 选中态背景 |
| `--accent-glow` | `rgba(30,91,255,0.22)` | 高亮 / hover ring |

> **可换色** — Tweaks 面板暴露 4 个候选: `X-KU Blue / Forest / Aubergine / Brass`。其余三色派生 token 在 JS 中实时计算。

### 1.5 编辑型辅色 (低频使用)

| Token | 值 | 用途 |
|---|---|---|
| `--gold` / `--gold-soft`   | `#A8843B` | 中优先级、修订 (modified) |
| `--rose` / `--rose-soft`   | `#B5432F` | 警示、删除 (removed)、高优 |
| `--moss` / `--moss-soft`   | `#4F6E3B` | 新增 (added)、低风险 |

> 三色使用频率不高，主要出现在 chip / 状态徽标 / diff 着色。**永远不用作大面积背景**。

---

## 2. 字体系统

### 2.1 字体栈

| Token | 字体 | 中文 fallback | 用途 |
|---|---|---|---|
| `--serif-display` | **Instrument Serif** | 思源宋体 (Noto Serif SC) | 大号标题、意大利体声明 |
| `--serif-body`    | **Source Serif 4** | 思源宋体 | 正文、长文阅读 |
| `--sans`          | Geist / -apple-system 栈 | PingFang SC / 微软雅黑 | UI 标签、按钮、meta |
| `--mono`          | **JetBrains Mono** | — | 文号、ID、时间戳、数字徽标 |

> **关键决策**：Instrument Serif 的意大利体 (`<em>`、`font-style:italic`) 是整个产品的"声音"。所有大号声明性文字 (Home headline、版本号、章节编号) 都倾向走斜体。

### 2.2 Type ramp

| Class | 字体 | 大小 / 行高 | 用途 |
|---|---|---|---|
| `.t-display`        | display, italic | 自定义 | 视觉锚点 (60–96px) |
| `.t-display-roman`  | display | 自定义 | 副标题 / 正体大号 |
| `.t-h1`             | display | 44 / 1.05 | 页面标题 |
| `.t-h2`             | display | 30 / 1.10 | 章节标题 |
| `.t-h3`             | serif-body 500 | 18 / 1.30 | 卡片标题 |
| `.t-body`           | serif-body | 16 / 1.70 | 正文 |
| `.t-meta`           | sans · uppercase 0.08em | 11 / 1.4 | 标签条 |
| `.t-meta-cap`       | sans · uppercase 0.14em 500 | 10 / 1 | 章节标记 ( `§ 01 探询` 这类) |
| `.t-mono`           | mono | 11 | 文号、时间、ID |
| `.t-label`          | sans 500 | 12 | 表单标签 |

### 2.3 中文排版规则

- 中文正文一律用 `"Noto Serif SC"` (思源宋体)，配 `letter-spacing: 0.005em` 与 `text-align: justify; text-justify: inter-ideograph;` 做两端齐。
- 标题级文字混用衬线; 数字用 Instrument Serif italic (`.num` 类) — 形成"中文楷正 + 数字斜体"的鲜明节奏。
- 不使用全角空格作排版手段; 用 hairline、`§` 标记、栅格间距分组。

---

## 3. 间距与栅格

### 3.1 基本变量

```css
--gutter: 32px;     /* 主栅格间距 (compact: 20px) */
--rail-w: 64px;     /* 左栏宽 */
--topbar-h: 56px;   /* 顶栏高 */
```

### 3.2 页面级版心

| 页面类型 | 最大宽度 | 边距 |
|---|---|---|
| 默认编辑型页面 (Home / Search / Compare / Compliance / Task) | 1440px | `padding: 32px 48px 48px` |
| 阅读型 (LawDetail)   | 1380px,内部三栏 240 / 1fr / 280 | 阅读列 56px 边距 |
| 对话型 (AIChat)      | 占满全 viewport · 三栏 220 / 1fr / 340 | 各栏内 padding |

### 3.3 节奏

- **章节** 之间常用 `border-top: 1px solid var(--ink)` (粗墨线) + 30–40px 上下气口。
- **小节** 用 `border-bottom: 1px solid var(--rule)` (细线) + 20–24px。
- **卡片** 用 1px rule, `border-radius: 4px` (从不超过 6px);最克制的圆角。

### 3.4 Density Tweak

`compact` 模式将 `--gutter` 由 32 降到 20；不收缩字号 (保持可读)，只压栅格。

---

## 4. 几何母题

源自 logo: 立方体外壳 + 中央钻石。整套设计把它解构成 3 个可复用元素。

### 4.1 钻石 (Diamond)

```jsx
<Diamond size={8} color="var(--accent)" />
```

用途：
- **章节锚点** — `§` 标记和 hr 中间
- **TOC 当前项** — 章节目录的 active 指示
- **风险/状态点** — 在主体卡 / 任务列表用作 risk dot
- **时间轴节点** — 版本沿革的每个时间点 (旋转 45° 的方块)
- **chip 头部** — `<Diamond/> 核心义务` 类前缀

### 4.2 立方体装饰 (CubeOrnament)

```jsx
<CubeOrnament size={280} spin={true} />
```

旋转的层叠立方体 SVG。永远以 `opacity: 0.12-0.20` 出现在深色面板的右上角或卡片背景。**禁止以高对比度出现在主区域**——它是装饰，不是焦点。

### 4.3 X-KU Logo

简化版 logo 出现在三处：
- 左栏顶端 (26px, animated)
- 加载图 (bundler 启动画面)
- 深色 spotlight 卡片背景 (大尺寸低透)

---

## 5. 组件库

### 5.1 按钮 (Button)

| 类 | 形态 |
|---|---|
| `.btn`         | 默认 — 白底 / 深灰边 / pill |
| `.btn-primary` | 深色 ink 底 + 白字 |
| `.btn-accent`  | 强调蓝底 + 白字 |
| `.btn-ghost`   | 无边框 + hover 时露 accent-soft |
| `.btn-sm`      | 26px 高 / 11px 字 |

所有按钮 32px 高 (sm 26px), `border-radius: 100px` (pill)，字号 12px sans。

### 5.2 Chip

| 类 | 视觉 |
|---|---|
| `.chip` (默认) | paper-sunk 底 + ink-2 字 |
| `.chip-accent` | accent-soft 底 + accent-deep 字 |
| `.chip-gold` / `.chip-rose` / `.chip-moss` | 对应辅色 soft 底 |
| `.chip-outline` | 透明底 + rule-strong 边 |

高度统一 22px (大) / 18px (微), 11px / 10px 字。

### 5.3 Card

```css
.card {
  background: var(--paper-card);
  border: 1px solid var(--rule);
  border-radius: 4px;
}
```

> **没有 box-shadow**。所有"提升"用 hairline + 内白底完成。

### 5.4 Highlight (`.hl`)

```css
background: linear-gradient(180deg, transparent 60%, var(--accent-glow) 60%);
```

文字下方的"标记笔"高亮 — 用于检索命中、AI 引用回溯、关键义务词。这是整个产品最有辨识度的微观元素之一。

### 5.5 Marginalia (边注)

```css
.marginalia {
  font-family: var(--serif-body);
  font-style: italic;
  font-size: 13px;
  border-left: 2px solid var(--accent);
  padding-left: 12px;
  color: var(--ink-3);
}
```

法规阅读页右栏专用 — 是从纸书继承来的"页边批注"概念。

### 5.6 Global Search (.global-search)

顶栏的搜索胶囊。32px 高, 圆角 100px, 默认 paper-2, focus 时变白底 ink 边, 右侧带 `⌘K` kbd 提示。

### 5.7 KBD

```css
font-family: mono; font-size: 10px;
padding: 2px 5px; border-radius: 3px;
background: paper-sunk; color: muted;
```

仅用于键盘提示 (⌘K、⌘↵)。

---

## 6. 数据可视化原则

不使用第三方图表库；所有 viz 手写 SVG，遵循下列规则：

1. **没有装饰性背景** — 网格用 1px dashed rule, 不用色块。
2. **数字字体走 mono** — 坐标轴、读数全部 JetBrains Mono。
3. **正色 = accent (蓝)**, 异常 = rose / gold, 中性 = ink。
4. **轴标签用 sans, uppercase letter-spacing 0.08em** — 像新闻图表。
5. **NOW 标记** — 当前时间用 `1px dashed ink` 配 `NOW` 字样, 不用颜色块。

具体已有的可视化：
- **检索 · Level × Time 矩阵** — 5 行 × 9 列散点;气泡半径表示命中数。
- **合规 · Time River** — 月度堆叠柱 (done / inprog / overdue), 底部 NOW 标记。
- **合规 · Risk Gauge** — SVG 圆环 + 百分比。
- **主体卡 Sparkline** — 单线 + 末点 dot, 颜色按 risk 染色。
- **版本对比 · Diff** — 字符级 LCS, accent-soft 底高亮 add, rose-soft 划掉 del。

---

## 7. 页面架构

```
左 64px Rail (icon-only)  +  顶 56px Topbar  +  Main (overflow:auto)
```

7 个核心页面：

| § | Route | 角色 | 设计要点 |
|---|---|---|---|
| 01 | `/home`        | 工作台   | 编辑型 masthead · 4 栏 highlight · spotlight · 三栏 (alerts / pulse / copilot) · 4 栏热点 |
| 02 | `/search`      | 检索结果 | 大斜体输入 · 层级×时间散点 · 编号化结果列表 |
| 03 | `/law`         | 法规阅读 | 三栏 (TOC / 阅读 / marginalia) · 大号意大利体条款号 · 钻石时间轴 |
| 04 | `/compare`     | 版本对比 | 4 大数字摘要 · 编辑引言 · 条款 tab · palimpsest diff |
| 05 | `/ai`          | AI 问答  | 三栏 (sessions / opinion / evidence ledger) · 上标引用↔右侧账册联动 |
| 06 | `/compliance`  | 合规     | 大标题 · 78 整体指数 · time river · 主体 tile + sparkline · 任务列表 |
| 07 | `/task`        | 任务详情 | 工序阶梯 · 法律依据卡 · 证据状态表 · AI 复核黑卡 · 活动时间线 |

### 7.1 Home 的版式语言

Home 是整套设计的"声明"。布局 anchors：

- **Masthead** — 巨号意大利体宣言句（`今日，7 项立法动向，3 条预警等待裁断。`），右上角日期 + Issue №。
- **Highlights strip** — 4 等分 highlight, 数字 italic 44px，无边框。
- **Hero 双栏** — 左:语义化提问框 + 三种检索模式;右:深色 spotlight 卡 (新闻头版式)。
- **三栏** — Alerts 编号化新闻列表 + Pulse 78 指数卡 + Copilot 引言卡。
- **底部 ribbon** — 4 个热点专题如同期刊索引。

### 7.2 LawDetail 的"阅读节奏"

```
[Meta bar  -  Title  -  Stats]   ← 56px hairline
[Version Lineage Timeline]       ← 5 节点钻石
[TOC | 阅读列 | 边注]
```

阅读列规则：
- 每条 `<article>` 默认无边框；激活态左侧 `2px solid accent` + `accent-soft` 底。
- 条款号 (`第二十一条`) 用 22px italic display, 义务条款变色为 accent。
- 文末用 `Diamond + END OF DOCUMENT` 字样做尾注。
- 右栏 marginalia 包括：编辑批注、AI 摘要黑卡、"引用此条的文件" 列表。

### 7.3 AIChat 的"法律意见书"

- 左 220 sessions
- 中 opinion-style:
  - 大斜体 title (`关于跨境电商订单数据出境的合规义务。`)
  - TL;DR 大块引文 (3px solid accent 左 border)
  - 编号步骤 (`01 02 03`...) — 每个 step body 内的引用是 `<sup>` 圆形徽标
- 右 340 evidence ledger:
  - 引用卡 stacked; active 项展开 excerpt + "跳转正文"
  - 底部综合置信 92% 黑卡 (带 cube 装饰)

### 7.4 Compare 的"羊皮纸"

- 顶部 4 大统计 + 编辑解读引文
- 条款 tab strip (active 顶部 2px accent)
- 双栏对比 spread: 旧 / 中间 gutter (含钻石 + change chip) / 新
- 文本用 LCS 字符级 diff: add = moss-soft 底, del = rose-soft 底 + 划掉

### 7.5 Compliance & Task 的"仪表盘 vs brief"

Compliance 强调宏观俯瞰 — 大数字、River、Subject grid。
Task 强调微观档案 — 工序阶梯、依据卡、证据状态、AI 复核、活动 timeline。

两者共享的 ruleset：
- 高优 = rose, 中 = gold, 低 = outline / moss
- progress bar 高 4–6px, 圆角 100px
- 时间戳一律 mono

---

## 8. 交互与微动效

### 8.1 易动 (motion)

```css
--ease:      cubic-bezier(0.2, 0.6, 0.2, 1);
--ease-soft: cubic-bezier(0.4, 0, 0.2, 1);
```

- 按钮 hover: `0.15s var(--ease)`，仅 border / bg
- Card hover: `border-color → ink` (不使用阴影)
- 页面切换: `pageIn 0.5s var(--ease)` — 透明 + 6px translateY 上推
- Cube 旋转: `60s linear infinite` — 极慢，几乎察觉不到

### 8.2 hover/active 模式

- Result list 行: hover → 背景 paper-2 (无其他)
- Citation chip (`<sup>`): hover → bg accent-glow
- Topbar 搜索: focus → 白底 ink 边

### 8.3 可点击的"叙事"

每条 alert / cite / chip / TOC item 都可点击；落点串起整个产品的内容图谱：

```
预警 → 法规阅读 → 条款 → AI 提问 → 证据账册 → 跳回正文
       ↓
   合规清单 → 任务 → 证据材料 → AI 复核
```

---

## 9. Tweaks (in-design controls)

右下角浮动面板，3 组：

| 组 | 控件 |
|---|---|
| 主题   | 强调色 (4 色 swatch) · 版心栅格开关 |
| 密度   | 紧凑 / 舒适 (segmented) |
| 跳转   | 7 个页面快捷按钮 |

> 强调色变换会派生 soft / glow / deep 三个 token，全局响应。版心栅格用 `.bg-grid` 类显示 32px 栅格 — 设计校验用。

---

## 10. 禁忌清单

为了避免落入"AI slop"和"老 ERP"窠臼，明令禁止以下：

- ❌ 渐变背景 (除非是 .hl 这种功能性下划线)
- ❌ 玻璃拟态 / 磨砂 / 阴影泛滥
- ❌ Emoji (logo / brand 表达除外)
- ❌ Inter / Roboto / Arial / 系统默认字体作为标题
- ❌ 大圆角 (≥8px) 卡片
- ❌ 装饰性 SVG 插画 (人物、场景、icon set) — 实在没素材就放占位
- ❌ 多色彩饱和图表 (堆叠饼图、彩虹柱)
- ❌ 三种以上正/辅色同屏出现
- ❌ 满屏深色面板 (深色用于 spotlight / inset, 不作主背景)
- ❌ 中英混排不留空格 (中英之间用半角空格隔开)

---

## 10.5 加载与骨架屏 (Loading / Skeleton)

数据请求与路由 chunk 加载时，**必须保留最终版式占位**，避免「0 → 真实数字」跳变、表格整块消失、或旧数据与小号「正在读取」并存。

### 组件

| 组件 | 用途 |
|------|------|
| `Skeleton` | 单行/统计数字/块占位 (`variant: text \| stat \| block`) |
| `SkeletonList` | 列表行 (`variant: line \| search`) |
| `SkeletonTable` | 表格式页，固定行高 |
| `PageSkeleton` | 整页布局 (`layout: generic \| home \| law \| compare \| table`) |
| `PageState` | 仅错误提示；`mode: inline \| overlay \| slot` |

### 规则

1. **列表/表格**：`loading` 时只显示 skeleton，不渲染上一轮 `v-for` 数据。
2. **工作台 highlights**：加载中用 `Skeleton variant="stat"`，不显示 `0`。
3. **法规详情**：首屏 `PageSkeleton layout="law"`；切换版本用 `articlesLoading` 局部骨架。
4. **版本对比**：首屏 `layout="compare"`；`diffLoading` 时 diff 区 skeleton，控件保持可见。
5. **路由**：`AppShell` 内 `RouterView` + `Suspense` + `PageSkeleton generic`。
6. **动画**：`sk-pulse` 约 1.2s；`prefers-reduced-motion: reduce` 时静态灰块。
7. **字体**：Google Fonts 使用 `display=optional`，减轻 FOUT。

---

## 11. 工程结构

```
index.html               ← 入口 (build.js 生成的 self-contained 单文件)
build.js                 ← 把所有 JSX + CSS 内联到 index.html
styles.css               ← 设计 token + 全局类
src/
  data.jsx               ← 所有 mock 数据 (window.XKU_DATA)
  brand.jsx              ← XKULogo · CubeOrnament · Diamond · CoreThread
  shell.jsx              ← Rail · Topbar · ROUTES
  mount.jsx              ← App 路由 + Tweaks 注入
  pages/
    Home.jsx
    Search.jsx
    LawDetail.jsx
    Compare.jsx
    AIChat.jsx
    Compliance.jsx
    Task.jsx
tweaks-panel.jsx         ← TweaksPanel + 控件
```

修改任何 jsx / css 后运行 `build.js` 重新打包 index.html。

---

## 12. 复刻清单 (给开发者)

一个新页面 / 新模块要"看起来像 X-KU"，按下列清单 self-check：

- [ ] 章节起头有 `§ 0X · UPPERCASE` 标记
- [ ] 数字、日期、ID 走 mono
- [ ] 大号声明用 Instrument Serif italic
- [ ] 中文正文 Noto Serif SC + justify
- [ ] 分隔用 1px hairline, 不要 shadow
- [ ] 至少出现一次钻石 (Diamond)
- [ ] 状态色不超过同屏 2 种 (主要正 + 1 辅)
- [ ] 按钮 / chip 走 pill (radius 100px)
- [ ] 卡片 radius ≤ 4px
- [ ] 没有 emoji、没有渐变、没有阴影
