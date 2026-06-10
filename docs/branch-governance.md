# 分支治理记录

记录时间：6.9

本文档只记录当前需要团队统一认知的分支状态，避免继续新增含义不清的长期分支。实际开发规则以 `README.md` 中的分支策略为准。

## 当前长期分支

- `master`：稳定发布分支，暂不直接开发。
- `feature`：当前集成分支，日常功能、修复、测试和文档都应先合入这里。

## 已合入 `feature` 的历史分支

这些远程分支的提交已经包含在 `feature` 中。课程验收前建议先保留，作为协同开发过程证据；验收结束后再统一清理。

- `origin/feature-database`
- `origin/github-actions-ci`
- `origin/copilot/add-issue-template-course-task`

## 需要单独处理的分支

### `origin/test`

状态：未合入 `feature`。

原因：该分支新增了后端测试，但同时改动了当前 `feature` 中的部分业务代码。不能直接 merge，否则可能回退现有能力系统、控制器和玩家逻辑。

建议处理方式：

- 先从 `feature` 新建 `test/backend-regression-tests`。
- 只迁移可用测试代码和必要测试依赖。
- 迁移后运行 `backend` 下的 `mvn test`。
- 通过 PR 合回 `feature`。

### `origin/master`

状态：与 `feature` 分叉。

原因：`master` 当前更接近课程模板初始分支，只包含独立的 deadline 提交，不应把当前 `feature` 直接强推或随意覆盖。

建议处理方式：

- 等 `feature` 通过稳定化 PR 后，再发起 `feature -> master` 的发布集成 PR。
- 合入方式优先使用普通 merge PR，保留集成历史。

## 新分支命名

- 功能：`feat/具体功能`
- 修复：`fix/具体问题`
- 测试：`test/测试范围`
- 文档：`docs/文档范围`

不再新增裸名长期分支，例如 `database`、`test`、`github-actions-ci`。
