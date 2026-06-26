# 参与贡献 Contributing

感谢你对 **X-KU 法规智询 (x-ku-law)** 的关注！本文档说明如何在本地搭建、提交改进并通过校验。

## 行为准则

参与本项目即表示你同意遵守 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)。

## 开发环境

- JDK 17
- Maven 3.8+
- Node.js 18+，pnpm
- Docker & Docker Compose

详细的本地启动步骤见 [README 的「快速开始」](README.md#快速开始)。简要流程：

```bash
# 1. 启动基础设施
docker compose up -d mysql redis rabbitmq elasticsearch

# 2. 复制环境变量模板并按需填写（切勿提交 .env）
cp .env.example .env

# 3. 启动后端
mvn spring-boot:run -pl lr-server -am -Dspring.profiles.active=dev

# 4. 启动前端
cd x-ku-law-web && pnpm install && pnpm dev
```

## 提交前自检

提交 PR 前，请确保相关校验通过：

```bash
# 后端测试
mvn test

# 前端类型检查
cd x-ku-law-web && pnpm exec vue-tsc --noEmit

# 前端 lint
cd x-ku-law-web && pnpm exec eslint .
```

## 分支与提交

- 从 `main` 切出特性分支，例如 `feat/xxx`、`fix/xxx`、`docs/xxx`。
- 提交信息建议遵循 [Conventional Commits](https://www.conventionalcommits.org/)（`feat:` / `fix:` / `docs:` / `refactor:` / `test:` / `chore:`）。
- 一个 PR 聚焦一件事，附上动机与必要的测试说明；涉及界面改动请附截图。

## 数据库迁移

数据库结构由 `lr-server/src/main/resources/db/migration/` 下的 Flyway 脚本管理，是唯一的结构来源。

- 新增变更请追加新的 `V<n>__<描述>.sql`，**不要修改已发布的历史迁移**。
- 迁移在应用启动时自动执行；本项目不提供回滚脚本，升级前请自行备份数据库。

## 欢迎的方向

- 法规数据源适配
- 条款解析和版本对比质量
- 搜索相关性和筛选体验
- AI 引用质量和回答反馈
- 后台流程和运维可观测性
- 前端阅读、检索和对比体验

## 安全问题

请勿通过公开 issue 报告安全漏洞，参见 [SECURITY.md](SECURITY.md)。
