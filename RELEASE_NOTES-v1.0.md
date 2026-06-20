# v1.0 稳定验收版本

## 版本定位
本版本为软件工程实训任务二的最终稳定发布版本，用于课程验收、答辩展示和项目归档。

## 主要功能
- Spring Boot + Vue 前后端分离架构。
- 用户注册、登录、存档读取和保存。
- 房间探索、方向移动、返回上一房间、传送房间。
- 物品拾取、丢弃、查看和背包负重限制。
- 理智增强剂机制，当前探索中提升最大负重。
- 可视化游戏界面，包含自由移动、地图、操作日志、帮助信息、背景音乐和基础音效。
- MySQL 数据库初始化脚本和后端数据持久化支持。

## 工程质量
- 后端 Maven test 通过（150 tests, 0 failures, 4 skipped）。
- 后端 Maven package 通过。
- 前端 npm lint 通过。
- 前端 npm build 通过。
- GitHub Actions 覆盖后端测试、后端打包、前端 lint 和前端构建。

## 发布产物
- `kai-fa-arknights-backend-v1.0.jar` — Spring Boot 可执行 jar（47 MB）
- `kai-fa-arknights-frontend-v1.0.zip` — Vue 生产构建 dist 压缩包（183 MB）
- `ability_schema-v1.0.sql` — MySQL 初始化脚本（29 KB）

## 运行说明
1. 使用 `ability_schema-v1.0.sql` 初始化 MySQL 数据库。
2. 根据本地环境检查 `backend/src/main/resources/application.yml` 中的数据库连接配置。
3. 启动后端 jar：`java -jar kai-fa-arknights-backend-v1.0.jar`。
4. 部署前端 dist 产物（解压 zip 并通过 Nginx 或其他静态服务器托管）。

## 已知限制
- 前后端仍需分别启动或部署，不提供一体化安装器。
- 数据库连接配置依赖本地 MySQL 环境，需根据实际情况修改 `application.yml`。
- 4 个 MySQL schema 验证测试依赖 Docker/Testcontainers 环境，WSL 中未运行 Docker 时自动跳过，不影响业务功能。
- 本版本面向课程验收，不提供完整商业化安装器。

## 本地验证结果
| 验证项 | 命令 | 结果 |
|--------|------|------|
| 后端测试 | `mvn -B clean test` | ✅ BUILD SUCCESS (150 tests, 0 failures, 4 skipped) |
| 后端打包 | `mvn -B package -DskipTests` | ✅ BUILD SUCCESS (zuul-1.0.0.jar) |
| 前端依赖安装 | `npm ci` | ✅ 925 packages installed |
| 前端代码检查 | `npm run lint` | ✅ No lint errors found |
| 前端生产构建 | `npm run build` | ✅ Build complete |
