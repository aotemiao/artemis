## 1. 结构调整

- [x] 1.1 从根 POM 移除 `artemis-api` 模块
- [x] 1.2 删除 `artemis-api` 聚合模块文件
- [x] 1.3 从 `artemis-dependencies` 移除 `artemis-api-*` 依赖管理
- [x] 1.4 将 `artemis-auth` 切换到 `artemis-system-client`

## 2. Harness 资产

- [x] 2.1 修改 `scripts/dev/new-domain-service.sh`，不再生成 API bridge
- [x] 2.2 修改服务目录字段为 `client_module`
- [x] 2.3 修改服务目录和脚手架守门脚本

## 3. 文档与规范

- [x] 3.1 更新 `README.md` 与 `ARCHITECTURE.md`
- [x] 3.2 更新新增领域服务 runbook 与项目进度文档
- [x] 3.3 更新 `openspec/specs/repository-structure/spec.md`
- [x] 3.4 保留本 OpenSpec change 轨迹

## 4. 验证

- [x] 4.1 执行 `scripts/harness/check-domain-service-scaffold.sh`
- [x] 4.2 执行 `scripts/harness/check-service-catalog.sh`
- [ ] 4.3 执行 `scripts/harness/verify-changed.sh working-tree`（治理检查已通过，Maven scoped verify 因当前环境缺少可执行的 Linux `java` 阻塞）
