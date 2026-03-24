# 凭证请求 DTO 重复问题

Status: archived
Last Reviewed: 2026-03-23
Review Cadence: 90 days

## 背景

`artemis-auth` 的登录请求 DTO 与 `artemis-system` 内部认证请求 DTO 在字段和校验规则上完全重复，已被重复模式扫描脚本识别为重复实现。

## 处理结果

- 删除 `artemis-auth` 本地 `LoginRequest`
- 删除 `artemis-system-adapter` 本地 `ValidateCredentialsRequest`
- 统一复用 `artemis-system-client` 中的 `ValidateCredentialsRequest`

## 关闭条件

- 重复模式扫描不再报该重复项
- `auth` 与 `system` 相关测试继续通过

## 验证

- `scripts/harness/check-duplicate-patterns.sh`
- `mvn -pl artemis-auth,artemis-modules/artemis-system/artemis-system-adapter -am test`

## 关闭日期

- `2026-03-23`
