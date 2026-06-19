# 明日方舟主题 World of Zuul 小组项目

[![CI](https://github.com/wutcst/kai-fa-arknights/actions/workflows/ci.yml/badge.svg)](https://github.com/wutcst/kai-fa-arknights/actions/workflows/ci.yml)

本项目是软件工程实训任务二的小组协同开发项目，基于 `world-of-zuul` 文字冒险样例扩展为前后端分离的可视化冒险游戏。项目围绕房间探索、物品拾取、背包负重、传送房间、用户登录注册、存档读档和图形化界面等功能进行迭代，并通过 GitHub Actions 验证后端测试、后端打包和前端构建。

## 功能概览

- 前后端分离架构：后端提供 Spring Boot API，前端提供 Vue 图形化界面。
- 地图与房间系统：支持多个房间、方向移动、返回上一房间和传送房间。
- 玩家与背包系统：玩家可拾取、丢弃、查看物品，背包受重量上限约束。
- 理智增强剂机制：吃掉特定物品后可提升玩家最大负重。
- 用户与存档系统：支持登录注册，并通过数据库保存和读取游戏状态。
- 自动化工程流程：CI 覆盖 Maven 测试、Maven 打包、前端依赖安装、前端 lint 和前端构建。

## 技术栈

- 后端：Java 17、Spring Boot 3.2、Spring Web、Spring Data JPA、H2、MySQL Driver、Maven
- 前端：Vue 3、Vue CLI、Axios、npm
- DevOps：GitHub Actions

## 目录结构

```text
backend/                 Spring Boot 后端服务
frontend/                Vue 前端应用
.github/workflows/       GitHub Actions CI 配置
.github/ISSUE_TEMPLATE/  课程任务 issue 模板
```

## 本地运行

### 后端

```bash
cd backend
mvn test
mvn package
mvn spring-boot:run
```

后端默认读取 `backend/src/main/resources/application.yml` 中的数据库和服务配置。需要连接 MySQL 时，请先确认本地数据库、账号和密码与配置一致。

数据库初始化只使用最新的 [`backend/sql/ability_schema.sql`](backend/sql/ability_schema.sql)。该脚本包含用户、存档、能力配置和用户能力表，并与当前后端实体字段保持一致；旧的部分初始化脚本已移除，避免误用不完整表结构。

### 前端

```bash
cd frontend
npm ci
npm run serve
```

生产构建：

```bash
cd frontend
npm run build
```

## CI / 持续集成

仓库已配置 GitHub Actions 工作流：

- 后端：`mvn -B test`、`mvn -B package`
- 前端：`npm ci`、`npm run lint`、`npm run build`
- 产物：后端 jar 与前端 `dist`

长期触发分支为：

- `master`
- `feature`

PR 目标分支为 `master` 或 `feature` 时也会触发 CI。

## 分支策略

当前课程验收阶段采用如下分支角色：

- `master`：最终稳定发布分支，当前已包含可验收项目版本。
- `feature`：课程协作和集成过程分支，保留用于展示开发、PR、测试和集成历史。
- `feat/*`：功能开发分支，从 `feature` 新建，完成后 PR 回 `feature`。
- `fix/*`：缺陷修复分支，从 `feature` 新建，完成后 PR 回 `feature`。
- `test/*`：测试补充分支，从 `feature` 新建，完成后 PR 回 `feature`。
- `docs/*`：文档维护分支，从 `feature` 新建，完成后 PR 回 `feature`。

`master` 已通过发布快照方式集成当前稳定项目版本。课程验收结束前不建议删除 `feature` 或历史工作分支，用于保留协同开发过程证据；验收结束后再统一清理临时分支。

当前历史分支处理清单见 [docs/branch-governance.md](docs/branch-governance.md)。

## 课程任务说明

本仓库用于完成“软件工程实训任务二：小组协同开发”。课程要求包括：

- 创建软件开发小组并在 GitHub 平台协作。
- 讨论并确定功能扩充点，形成完整交互界面和功能逻辑。
- 使用 issue 管理任务拆分、分配和里程碑。
- 基于分支模型完成开发、测试、提交、归并和同步。
- 提交代码后通过自动化规范检查、测试和打包流程验证质量。
- 在项目根目录提交实训报告文件，并完成课程答辩展示。

允许使用 AI 辅助开发和设计，但需在报告中说明使用的模型和辅助完成的工作内容。
