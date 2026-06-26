# 法规智询 — 用户手册

**版本：** 1.0.0  
**适用系统：** x-ku-law（Legal Regulation Platform）  
**最后更新：** 2026-05-23

---

## 目录

1. [平台简介](#1-平台简介)
2. [适用对象与角色](#2-适用对象与角色)
3. [快速入门](#3-快速入门)
4. [登录与账号安全](#4-登录与账号安全)
5. [法规检索与浏览](#5-法规检索与浏览)
6. [个人工作台](#6-个人工作台)
7. [法规订阅与预警](#7-法规订阅与预警)
8. [AI 法规助手](#8-ai-法规助手)
9. [消息通知](#9-消息通知)
10. [法规内容管理（管理员）](#10-法规内容管理管理员)
11. [系统管理（管理员）](#11-系统管理管理员)
12. [API 调用规范](#12-api-调用规范)
13. [常见问题](#13-常见问题)
14. [附录](#14-附录)

---

## 1. 平台简介

### 1.1 平台定位

法规智询（x-ku-law）是一套面向企业法务、合规团队及研究人员的**法规知识管理与检索系统**。平台提供：

- **法规主数据管理**：法规文件、版本、条款、分类、关联关系
- **全文检索**：基于 Elasticsearch 的关键词与条件组合检索
- **个人工作台**：收藏、保存检索、浏览反馈
- **法规订阅**：按关键词/条件订阅法规变更，接收命中预警
- **AI 辅助**（逐步开放）：法规问答会话与引用追溯
- **多租户与 RBAC**：租户隔离、角色权限精细控制

### 1.2 当前版本能力说明

| 能力域 | 状态 | 说明 |
|--------|------|------|
| 认证与 RBAC | ✅ 已上线 | 多租户登录、JWT、角色权限 |
| 法规浏览与管理 | ✅ 已上线 | 文档/版本/条款/分类/关系 |
| 全文检索 | ✅ 已上线 | 需启用 Elasticsearch |
| 保存检索 / 收藏 / 反馈 | ✅ 已上线 | 登录用户可用 |
| 法规订阅 | ✅ 已上线 | 规则与命中记录 |
| AI 会话历史 | ✅ 已上线 | 查看/删除历史会话 |
| AI 智能问答 | 🔜 二期 | `POST /ai/messages/ask` 尚未开放 |
| 数据采集 | 🔜 二期 | 采集域 API 规划中 |
| 合规管理 | 🔜 二期 | 合规清单/任务/证据等 |
| 运营内容 / 客服 / 开放 API | 🔜 二期 | 数据库表已预留 |

> 本平台当前以 **REST API** 形式提供服务。前端界面可由 Swagger UI（开发环境）或第三方客户端对接使用。

### 1.3 访问地址

| 环境 | 地址 | 说明 |
|------|------|------|
| 开发环境 API 基址 | `http://localhost:8080` | 本地 `dev` profile |
| API 文档（开发） | `http://localhost:8080/swagger-ui.html` | 生产环境已关闭 |
| 健康检查 | `http://localhost:8080/actuator/health` | 运维监控 |

---

## 2. 适用对象与角色

### 2.1 用户类型

| 角色 | 典型用户 | 主要能力 |
|------|----------|----------|
| **游客** | 未登录访问者 | 公开法规全文检索 |
| **普通用户** | 法务专员、研究员 | 登录后浏览法规、收藏、订阅、反馈、查看通知 |
| **平台管理员** | 系统管理员、内容运营 | 法规 CRUD、用户/角色/权限/字典管理、发送通知 |

### 2.2 多租户说明

- 每个用户归属于一个**租户**（Tenant），登录时必须提供 `tenantCode`（租户编码）。
- 数据默认按租户隔离；公开法规对所有用户可见，租户私有法规仅本租户可见。
- 开发/测试环境预置租户编码：`platform`（平台租户）。

### 2.3 权限模型

平台采用 **RBAC（基于角色的访问控制）**：

```
用户 → 角色 → 权限码 → API 接口
```

- **读取类接口**（如法规列表、详情）：登录即可访问，无需额外权限。
- **写入/管理类接口**：需具备对应权限码（如 `law:document:create`）。
- 无权限时返回 HTTP 403，错误码 `403`。

---

## 3. 快速入门

### 3.1 游客：检索公开法规

无需登录，直接调用：

```http
GET /search/laws?keyword=劳动法&pageNo=1&pageSize=10
```

返回公开法规的分页检索结果。

### 3.2 普通用户：完整使用流程

**步骤 1 — 登录**

```http
POST /auth/login
Content-Type: application/json

{
  "tenantCode": "platform",
  "username": "your_username",
  "password": "your_password"
}
```

**步骤 2 — 携带 Token 访问**

```http
GET /law/documents?pageNo=1&pageSize=10
Authorization: Bearer {accessToken}
```

**步骤 3 — 常用操作**

| 操作 | 接口 |
|------|------|
| 检索法规 | `GET /search/laws` |
| 查看法规详情 | `GET /law/documents/{id}` |
| 收藏法规 | `POST /workspace/favorites` |
| 创建订阅规则 | `POST /subscription/rules` |
| 查看通知 | `GET /system/notifications/inbox` |

### 3.3 管理员：内容维护流程

1. 使用管理员账号登录（见 [4.4 默认测试账号](#44-默认测试账号)）。
2. **创建法规** → `POST /law/documents`
3. **创建版本** → `POST /law/versions`
4. **录入条款** → `POST /law/articles`
5. **发布版本** → `PUT /law/versions/{id}/publish`
6. **维护分类与关系** → `/law/categories`、`/law/relations`

---

## 4. 登录与账号安全

### 4.1 登录

**接口：** `POST /auth/login`

**请求体：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| tenantCode | string | 是 | 租户编码，如 `platform` |
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

**成功响应示例：**

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 7200
  }
}
```

| 字段 | 说明 |
|------|------|
| accessToken | 访问令牌，用于后续 API 调用，默认有效期 2 小时 |
| refreshToken | 刷新令牌，用于换取新的 accessToken，默认有效期 7 天 |
| expiresIn | accessToken 有效秒数 |

**常见登录错误：**

| 错误码 | 含义 | 处理建议 |
|--------|------|----------|
| 1000 | 用户不存在 | 检查用户名与租户 |
| 1001 | 用户名或密码错误 | 确认密码 |
| 1004 | 租户已禁用 | 联系管理员 |
| 1005 | 租户不存在 | 检查 tenantCode |
| 1006 | 用户已被禁用或锁定 | 联系管理员 |

### 4.2 刷新 Token

Access Token 过期前，使用 Refresh Token 换取新令牌：

```http
POST /auth/refresh
X-Refresh-Token: {refreshToken}
```

成功返回新的 `accessToken` 字符串。

### 4.3 登出

```http
POST /auth/logout
Authorization: Bearer {accessToken}
```

登出后 Token 立即从服务端失效，需重新登录。

### 4.4 默认测试账号

> ⚠️ **仅用于开发/测试环境，生产环境必须修改默认密码。**

| 项目 | 值 |
|------|-----|
| 租户编码 | `platform` |
| 用户名 | `admin` |
| 密码 | `Admin@123` |
| 角色 | 平台管理员（`platform_admin`） |
| 权限 | 全部 32 项管理权限 |

### 4.5 安全建议

- 生产环境必须使用随机生成的 `JWT_SECRET`（256-bit Base64）。
- 不要在客户端明文存储密码；Token 应存储在安全位置（HttpOnly Cookie 或安全存储）。
- 共享设备使用后请及时登出。
- 登录行为会记录到系统登录日志（IP、User-Agent）。

---

## 5. 法规检索与浏览

### 5.1 全文检索

**接口：** `GET /search/laws`  
**权限：** 游客可访问（仅返回公开法规）；登录用户额外可见本租户法规。

**查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 检索关键词 |
| effectLevel | string | 效力级别筛选 |
| status | string | 时效状态筛选 |
| publishAuthority | string | 发布机关筛选 |
| pageNo | int | 页码，默认 1 |
| pageSize | int | 每页条数，默认 10，最大 100 |

**示例：**

```http
GET /search/laws?keyword=数据安全&effectLevel=national&pageNo=1&pageSize=20
```

> **注意：** 开发环境若未启用 Elasticsearch（`app.search.enabled=false`），检索接口可能不可用。生产/完整环境需配置 `ES_URIS` 并设置 `SEARCH_ENABLED=true`。

### 5.2 法规文件浏览

**接口前缀：** `/law/documents`  
**权限：** 需登录

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/law/documents` | 支持 keyword、lawType、legalLevel、status 等筛选 |
| 详情 | GET | `/law/documents/{id}` | 单条法规元信息 |

**列表筛选参数：**

| 参数 | 说明 |
|------|------|
| keyword | 模糊匹配标题、文号 |
| lawType | 法规类型 |
| legalLevel | 效力级别 |
| status | 时效状态（如有效、废止） |
| regionCode | 适用地区代码 |
| issuingOrg | 发布机构（模糊） |

### 5.3 法规版本

**接口前缀：** `/law/versions`

| 操作 | 方法 | 路径 |
|------|------|------|
| 版本列表 | GET | `/law/versions` |
| 版本详情 | GET | `/law/versions/{id}` |

版本是法规的修订历史载体。一个法规文件可包含多个版本，每个版本有独立的发布日期、生效日期和状态。

### 5.4 法规条款

**接口前缀：** `/law/articles`

| 操作 | 方法 | 路径 |
|------|------|------|
| 条款列表 | GET | `/law/articles` |
| 条款详情 | GET | `/law/articles/{id}` |

条款支持层级结构（章、节、条），包含正文内容、义务标记、处罚标记等字段。

### 5.5 法规分类

**接口前缀：** `/law/categories`

| 操作 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页列表 | GET | `/law/categories` | 管理用 |
| 全部启用分类 | GET | `/law/categories/all` | 下拉/树形展示 |
| 分类详情 | GET | `/law/categories/{id}` | |

### 5.6 法规关系

**接口前缀：** `/law/relations`

用于表达法规之间的引用、修订、废止等关联关系。

| 操作 | 方法 | 路径 |
|------|------|------|
| 关系列表 | GET | `/law/relations` |
| 关系详情 | GET | `/law/relations/{id}` |

**关系类型示例：** 修订、引用、废止、配套等（具体值可通过字典配置）。

### 5.7 典型浏览路径

```
全文检索 → 定位法规 → 查看法规详情
                    → 查看当前有效版本
                    → 展开条款列表阅读正文
                    → 查看关联法规（关系）
                    → 收藏到个人工作台
```

---

## 6. 个人工作台

个人工作台模块帮助用户管理日常研究资料与使用反馈。所有接口需登录，数据按当前用户隔离。

### 6.1 收藏

**接口前缀：** `/workspace/favorites`

| 操作 | 方法 | 路径 |
|------|------|------|
| 收藏列表 | GET | `/workspace/favorites` |
| 添加收藏 | POST | `/workspace/favorites` |
| 取消收藏 | DELETE | `/workspace/favorites/{id}` |

**添加收藏请求体：**

| 字段 | 必填 | 说明 |
|------|------|------|
| refType | 是 | 引用类型，如 `law_document`、`law_article` |
| refId | 是 | 被收藏对象的 ID |
| folderName | 否 | 收藏夹名称，用于分组 |
| titleSnapshot | 否 | 标题快照，便于列表展示 |

**使用场景：** 将常用法规、重要条款加入收藏夹，便于快速回访。

### 6.2 保存检索

**接口前缀：** `/search/saved`

| 操作 | 方法 | 路径 |
|------|------|------|
| 列表 | GET | `/search/saved` |
| 新建 | POST | `/search/saved` |
| 更新 | PUT | `/search/saved/{id}` |
| 删除 | DELETE | `/search/saved/{id}` |

**新建请求体：**

| 字段 | 必填 | 说明 |
|------|------|------|
| name | 是 | 保存检索的名称 |
| keyword | 否 | 检索关键词 |
| filtersJson | 否 | 筛选条件 JSON |
| notifyEnabled | 否 | 是否开启变更通知 |
| status | 否 | 状态 |

**使用场景：** 将常用的检索条件保存为快捷入口，避免重复输入筛选条件。

### 6.3 意见反馈

**接口前缀：** `/workspace/feedbacks`

| 操作 | 方法 | 路径 |
|------|------|------|
| 反馈列表 | GET | `/workspace/feedbacks` |
| 提交反馈 | POST | `/workspace/feedbacks` |
| 删除反馈 | DELETE | `/workspace/feedbacks/{id}` |

**提交反馈请求体：**

| 字段 | 必填 | 说明 |
|------|------|------|
| feedbackType | 是 | 反馈类型（如内容纠错、功能建议） |
| content | 是 | 反馈内容 |
| refType | 否 | 关联对象类型 |
| refId | 否 | 关联对象 ID |

**使用场景：** 发现法规内容错误或平台功能问题时提交反馈。

---

## 7. 法规订阅与预警

订阅模块允许用户设定关注规则，当有新法规或变更命中规则时，系统生成**命中记录**供用户查看。

### 7.1 订阅规则

**接口前缀：** `/subscription/rules`

| 操作 | 方法 | 路径 |
|------|------|------|
| 规则列表 | GET | `/subscription/rules` |
| 新建规则 | POST | `/subscription/rules` |
| 更新规则 | PUT | `/subscription/rules/{id}` |
| 删除规则 | DELETE | `/subscription/rules/{id}` |

**新建/更新规则字段：**

| 字段 | 必填 | 说明 |
|------|------|------|
| ruleName | 是 | 规则名称 |
| ruleType | 否 | 规则类型 |
| keyword | 否 | 关注关键词 |
| filtersJson | 否 | 扩展筛选条件（JSON） |
| deliveryChannel | 否 | 推送渠道 |
| frequency | 否 | 推送频率 |
| status | 否 | 启用/停用 |

**使用示例：** 创建名为「数据合规关注」的规则，关键词设为「个人信息保护」，当相关法规更新时收到命中提醒。

### 7.2 订阅命中

**接口前缀：** `/subscription/matches`

| 操作 | 方法 | 路径 |
|------|------|------|
| 命中列表 | GET | `/subscription/matches` |
| 标记已读 | PUT | `/subscription/matches/{id}/read` |

**典型工作流：**

1. 创建订阅规则并启用。
2. 定期查看 `GET /subscription/matches` 获取新命中。
3. 阅读后将命中标记为已读：`PUT /subscription/matches/{id}/read`。

---

## 8. AI 法规助手

AI 模块用于法规智能问答与对话历史管理。**当前版本**主要支持会话与消息的历史查询；RAG 智能问答接口计划在二期开放。

### 8.1 会话管理

**接口前缀：** `/ai/sessions`

| 操作 | 方法 | 路径 |
|------|------|------|
| 会话列表 | GET | `/ai/sessions` |
| 会话详情 | GET | `/ai/sessions/{id}` |
| 删除会话 | DELETE | `/ai/sessions/{id}` |

### 8.2 消息记录

**接口前缀：** `/ai/messages`

| 操作 | 方法 | 路径 |
|------|------|------|
| 消息列表 | GET | `/ai/messages` |

支持按会话 ID 分页查询历史问答消息。

### 8.3 智能问答（规划中）

```
POST /ai/messages/ask   ← 二期开放
```

开放后将支持基于法规知识库的自然语言问答，回答附带引用来源。

---

## 9. 消息通知

### 9.1 用户收件箱

**接口前缀：** `/system/notifications`

| 操作 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 我的通知 | GET | `/system/notifications/inbox` | 登录即可 |
| 标记已读 | PUT | `/system/notifications/{id}/read` | 登录即可 |

用于接收系统广播、订阅提醒、管理员发送的通知等。

### 9.2 管理员发送通知

| 操作 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 通知列表 | GET | `/system/notifications` | `notification:list` |
| 发送通知 | POST | `/system/notifications` | `notification:send` |
| 删除通知 | DELETE | `/system/notifications/{id}` | `notification:send` |

**发送通知请求体主要字段：**

| 字段 | 说明 |
|------|------|
| title | 通知标题 |
| content | 通知正文 |
| notifyType | 通知类型 |
| targetType | 目标范围（如全体用户、指定用户） |

---

## 10. 法规内容管理（管理员）

以下操作需要对应权限码，平台管理员（`platform_admin`）默认拥有全部权限。

### 10.1 法规文件管理

**接口前缀：** `/law/documents`

| 操作 | 方法 | 路径 | 权限码 |
|------|------|------|--------|
| 新建 | POST | `/law/documents` | `law:document:create` |
| 更新 | PUT | `/law/documents/{id}` | `law:document:update` |
| 删除 | DELETE | `/law/documents/{id}` | `law:document:delete` |

**新建法规主要字段：**

| 字段 | 必填 | 说明 |
|------|------|------|
| lawUid | 是 | 法规唯一标识（全局不可重复） |
| title | 是 | 法规标题 |
| lawType | 是 | 法规类型 |
| status | 是 | 时效状态 |
| documentNo | 否 | 文号 |
| legalLevel | 否 | 效力级别 |
| issuingOrg | 否 | 发布机构 |
| regionCode | 否 | 适用地区 |
| publishDate | 否 | 发布日期 |
| effectiveDate | 否 | 生效日期 |
| expireDate | 否 | 失效日期 |
| summary | 否 | 摘要 |

### 10.2 版本管理

**接口前缀：** `/law/versions`

| 操作 | 方法 | 路径 | 权限码 |
|------|------|------|--------|
| 新建 | POST | `/law/versions` | `law:version:create` |
| 更新 | PUT | `/law/versions/{id}` | `law:version:update` |
| 发布 | PUT | `/law/versions/{id}/publish` | `law:version:publish` |
| 删除 | DELETE | `/law/versions/{id}` | `law:version:delete` |

**版本生命周期：**

```
草稿 → 编辑内容/条款 → 发布（publish）→ 对外可见
```

> 已发布的版本不可重复发布（错误码 2006）。

**新建版本主要字段：**

| 字段 | 必填 | 说明 |
|------|------|------|
| documentId | 是 | 所属法规 ID |
| versionNo | 是 | 版本号，如 `2024-修订` |
| versionName | 否 | 版本名称 |
| publishDate | 否 | 发布日期 |
| effectiveDate | 否 | 生效日期 |

### 10.3 条款管理

**接口前缀：** `/law/articles`

| 操作 | 方法 | 权限码 |
|------|------|--------|
| 新建 | POST | `law:article:create` |
| 更新 | PUT | `law:article:update` |
| 删除 | DELETE | `law:article:delete` |

**新建条款主要字段：**

| 字段 | 必填 | 说明 |
|------|------|------|
| documentId | 是 | 法规 ID |
| versionId | 是 | 版本 ID |
| contentText | 否 | 条款正文 |
| articleNo | 否 | 条号 |
| chapterNo / chapterTitle | 否 | 章节信息 |
| obligationFlag | 否 | 是否义务条款 |
| penaltyFlag | 否 | 是否处罚条款 |

### 10.4 分类管理

**接口前缀：** `/law/categories`

| 操作 | 方法 | 权限码 |
|------|------|--------|
| 新建 | POST | `law:category:create` |
| 更新 | PUT | `law:category:update` |
| 删除 | DELETE | `law:category:delete` |

支持树形分类（通过 `parentId` 建立父子关系）。

### 10.5 关系管理

**接口前缀：** `/law/relations`

| 操作 | 方法 | 权限码 |
|------|------|--------|
| 新建 | POST | `law:relation:create` |
| 更新 | PUT | `law:relation:update` |
| 删除 | DELETE | `law:relation:delete` |

**新建关系主要字段：**

| 字段 | 必填 | 说明 |
|------|------|------|
| sourceDocumentId | 是 | 源法规 ID |
| targetDocumentId | 是 | 目标法规 ID |
| relationType | 是 | 关系类型 |
| sourceArticleId | 否 | 源条款 ID |
| targetArticleId | 否 | 目标条款 ID |

### 10.6 内容维护最佳实践

1. **先建法规，再建版本，最后录入条款**，保持层级清晰。
2. **lawUid 全局唯一**，建议使用规范化编码（如 `CN-LAW-2024-001`）。
3. 版本发布前完成条款校对；发布后变更应通过新版本管理。
4. 删除操作为**逻辑删除**，数据可恢复（需 DBA 或后续功能支持）。
5. 所有写操作均记录操作日志（`@OperLog`），便于审计追溯。

---

## 11. 系统管理（管理员）

### 11.1 用户管理

**接口前缀：** `/system/users`

| 操作 | 方法 | 权限码 |
|------|------|--------|
| 用户列表 | GET | `system:user:list` |
| 用户详情 | GET | `system:user:list` |
| 新建用户 | POST | `system:user:create` |
| 更新用户 | PUT | `system:user:update` |
| 删除用户 | DELETE | `system:user:delete` |

**新建用户主要字段：**

| 字段 | 必填 | 说明 |
|------|------|------|
| username | 是 | 登录用户名 |
| password | 是 | 初始密码 |
| realName | 否 | 真实姓名 |
| mobile / email | 否 | 联系方式 |
| userType | 否 | 用户类型：normal、admin、operator 等 |
| status | 否 | enabled / disabled |

> 新建用户后，需通过角色管理为其分配角色，才能获得相应权限。

### 11.2 角色管理

**接口前缀：** `/system/roles`

| 操作 | 方法 | 权限码 |
|------|------|--------|
| 角色列表/详情 | GET | `system:role:list` |
| 新建角色 | POST | `system:role:create` |
| 更新角色 | PUT | `system:role:update` |
| 删除角色 | DELETE | `system:role:delete` |

**建议角色划分示例：**

| 角色 | 权限范围 |
|------|----------|
| platform_admin | 全部权限 |
| law_editor | 法规 CRUD + 发布 |
| law_viewer | 仅法规读取（默认登录即可） |
| system_operator | 用户/字典管理 |

### 11.3 权限管理

**接口前缀：** `/system/permissions`

| 操作 | 方法 | 权限码 |
|------|------|--------|
| 分页列表 | GET | `system:permission:list` |
| 完整权限树 | GET | `/system/permissions/all` |
| 新建/更新/删除 | POST/PUT/DELETE | 对应 create/update/delete |

### 11.4 字典管理

**接口前缀：** `/system/dict`

字典用于维护系统中各类下拉选项（法规类型、效力级别、关系类型等）。

**字典类型：**

| 操作 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 类型列表 | GET | `/system/dict/types` | 登录可读 |
| 新建类型 | POST | `/system/dict/types` | `system:dict:create` |
| 更新/删除类型 | PUT/DELETE | `/system/dict/types/{id}` | update/delete |

**字典数据：**

| 操作 | 方法 | 路径 | 权限 |
|------|------|------|------|
| 数据列表 | GET | `/system/dict/data` | 登录可读 |
| 按编码查全部 | GET | `/system/dict/data/list?dictCode=xxx` | 登录可读 |
| 新建/更新/删除 | POST/PUT/DELETE | `/system/dict/data` | 对应权限 |

**前端/客户端用法：** 页面加载时调用 `GET /system/dict/data/list?dictCode=law_type` 获取「法规类型」全部选项。

---

## 12. API 调用规范

### 12.1 统一响应格式

所有接口返回 `CommonResult<T>`：

**成功：**

```json
{
  "code": 0,
  "msg": "success",
  "data": { ... }
}
```

**失败：**

```json
{
  "code": 1001,
  "msg": "用户名或密码错误",
  "data": null
}
```

### 12.2 分页响应格式

列表接口的 `data` 为 `PageResult`：

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "list": [ ... ],
    "total": 128,
    "pageNo": 1,
    "pageSize": 10
  }
}
```

### 12.3 请求头

| 头名称 | 用途 | 示例 |
|--------|------|------|
| Authorization | 访问令牌 | `Bearer eyJhbGci...` |
| X-Refresh-Token | 刷新令牌（仅 refresh 接口） | `eyJhbGci...` |
| Content-Type | 请求体格式 | `application/json` |

### 12.4 HTTP 状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 请求成功（业务错误通过 code 字段表达） |
| 401 | 未登录或 Token 失效 |
| 403 | 已登录但权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 12.5 分页参数约定

| 参数 | 默认值 | 范围 |
|------|--------|------|
| pageNo | 1 | ≥ 1 |
| pageSize | 10 | 1 ~ 100 |

---

## 13. 常见问题

### Q1：登录时提示「租户不存在」

确认 `tenantCode` 拼写正确。开发环境使用 `platform`。

### Q2：接口返回 401

- Access Token 已过期 → 调用 `POST /auth/refresh` 刷新，或重新登录。
- 未携带 `Authorization` 头 → 除公开接口外均需登录。

### Q3：接口返回 403

当前账号缺少对应权限码。联系管理员分配角色/权限。

### Q4：全文检索不可用

检查 Elasticsearch 是否启动，环境变量 `SEARCH_ENABLED=true`，且 `ES_URIS` 配置正确。开发 profile 默认可能关闭检索。

### Q5：Swagger 打不开

生产环境 Swagger 已关闭（返回 404），请使用开发环境或查阅本文档 / OpenAPI 导出。

### Q6：如何创建普通用户？

管理员登录后：

1. `POST /system/users` 创建用户。
2. `POST /system/roles` 创建角色并绑定权限（或使用已有角色）。
3. 在用户更新接口中关联角色（具体字段见 Swagger 或 UserCreateDTO 扩展）。

### Q7：法规唯一标识冲突（错误码 2002）

`lawUid` 在系统中必须唯一，请更换标识后重试。

### Q8：版本无法重复发布（错误码 2006）

该版本已处于发布状态。如需变更，请创建新版本并发布。

---

## 14. 附录

### 附录 A：完整 API 索引

#### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/login` | 登录 |
| POST | `/auth/refresh` | 刷新 Token |
| POST | `/auth/logout` | 登出 |

#### 检索

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/search/laws` | 全文检索（公开） |
| GET | `/search/saved` | 保存检索列表 |
| POST | `/search/saved` | 新建保存检索 |
| PUT | `/search/saved/{id}` | 更新保存检索 |
| DELETE | `/search/saved/{id}` | 删除保存检索 |

#### 法规

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/law/documents` | 法规文件 |
| GET/POST/PUT/DELETE | `/law/versions` | 法规版本 |
| PUT | `/law/versions/{id}/publish` | 发布版本 |
| GET/POST/PUT/DELETE | `/law/articles` | 法规条款 |
| GET/POST/PUT/DELETE | `/law/categories` | 法规分类 |
| GET | `/law/categories/all` | 全部启用分类 |
| GET/POST/PUT/DELETE | `/law/relations` | 法规关系 |

#### 工作台

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/DELETE | `/workspace/favorites` | 收藏 |
| GET/POST/DELETE | `/workspace/feedbacks` | 反馈 |

#### 订阅

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/subscription/rules` | 订阅规则 |
| GET | `/subscription/matches` | 订阅命中 |
| PUT | `/subscription/matches/{id}/read` | 标记已读 |

#### AI

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/DELETE | `/ai/sessions` | AI 会话 |
| GET | `/ai/sessions/{id}` | 会话详情 |
| GET | `/ai/messages` | AI 消息 |

#### 系统

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/system/users` | 用户管理 |
| GET/POST/PUT/DELETE | `/system/roles` | 角色管理 |
| GET/POST/PUT/DELETE | `/system/permissions` | 权限管理 |
| GET | `/system/permissions/all` | 权限树 |
| GET/POST/PUT/DELETE | `/system/dict/types` | 字典类型 |
| GET/POST/PUT/DELETE | `/system/dict/data` | 字典数据 |
| GET | `/system/dict/data/list` | 按编码查字典 |
| GET/POST/DELETE | `/system/notifications` | 通知管理 |
| GET | `/system/notifications/inbox` | 我的通知 |
| PUT | `/system/notifications/{id}/read` | 标记已读 |

### 附录 B：权限码一览

| 权限码 | 说明 |
|--------|------|
| `system:user:list/create/update/delete` | 用户管理 |
| `system:role:list/create/update/delete` | 角色管理 |
| `system:permission:list/create/update/delete` | 权限管理 |
| `system:dict:create/update/delete` | 字典管理 |
| `notification:send` | 发送/删除通知 |
| `law:document:create/update/delete` | 法规文件 |
| `law:version:create/update/delete/publish` | 法规版本 |
| `law:article:create/update/delete` | 法规条款 |
| `law:category:create/update/delete` | 法规分类 |
| `law:relation:create/update/delete` | 法规关系 |

### 附录 C：业务错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 已过期 |
| 403 | 无访问权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1000-1006 | 认证相关（用户不存在、密码错误、租户问题等） |
| 1100-1102 | 系统相关（字典、配置、上传） |
| 2000-2006 | 法规相关（不存在、UID 重复、版本已发布等） |
| 3000-3001 | AI 相关 |
| 4000-4001 | 合规相关（二期） |
| 5000-5001 | 订阅/保存检索相关 |

### 附录 D：二期规划功能预览

以下功能已在数据库设计中预留，API 将在后续版本陆续开放：

| 模块 | 规划能力 |
|------|----------|
| 采集域 | 法规来源配置、自动采集、批量导入、数据质量检查 |
| 合规域 | 合规主体、义务清单、合规任务、证据管理、风险报告 |
| 运营内容 | 法规专题、Banner、帮助文章 |
| 客服 | 工单与消息 |
| 开放 API | API 客户端管理、访问日志、导出申请 |
| AI | RAG 智能问答 `POST /ai/messages/ask` |

---

## 文档维护

- 技术部署说明请参阅项目根目录 [README.md](../README.md)。
- 系统设计详见 `legal_regulation_platform_system_design_v2.docx`。
- API 变更以 Swagger（开发环境）及代码为准；本文档随版本迭代更新。

**反馈与建议：** 登录后通过 `POST /workspace/feedbacks` 提交，或联系平台管理员。
