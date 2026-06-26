# X-KU 法规智询 · Product Context

## Register

product

## Users

- 法务、合规、数据治理和业务负责人：需要快速定位法规、理解条款义务、追踪版本变化，并把判断沉淀为可复核证据链。
- 法规数据运营和平台管理员：需要接入法规来源、维护主数据、处理采集与解析任务、排查失败记录。
- 管理者和审计角色：需要看到关键预警、处理状态、引用依据和操作留痕。

## Product Purpose

X-KU 把法规检索、阅读、版本对比、AI 可溯源问答、订阅预警、收藏反馈和后台数据运维连接成一条工作流。界面必须让用户始终知道：正在看哪部法规、哪个版本、哪个条款、哪个证据来源，以及下一步能做什么。

## Experience Principles

- 证据优先：AI 结论、预警和合规判断都要回到法规正文、条款、版本或后台任务记录。
- 编辑型清晰：用期刊式排版组织密集法律信息，但交互控件保持产品 UI 的熟悉和高效。
- 前后台同源：后台可以更密，但仍使用同一套纸面、墨色、细线、低圆角、状态徽标和操作反馈。
- 少装饰，多落点：指标、卡片、chip 和列表项必须有真实含义或可点击路径。
- 状态完整：加载、空数据、错误、禁用、提交中、无权限和无最近法规都要有明确表达。

## Design Direction

The interface follows `DESIGN.md`: white paper surfaces, deep navy ink, electric-blue accent, editorial serif for display moments, sans for controls, mono for IDs/time/numbers, hairline separation, 4px cards, pill buttons/chips, and restrained diamond/cube motifs.

Avoid generic SaaS gradients, decorative glass, large-radius cards, ornamental illustrations, gratuitous motion, and fake dashboard precision.

## Current Upgrade Goal

Upgrade the existing Vue app against the prototype direction without changing backend APIs, routes, or permission rules. Prioritize real workflow usefulness, page/component consistency, and a reliable validation baseline.
