# Artemis System Client 契约

本文件描述 `artemis-system-client` 当前对外发布的内部契约，并作为 `scripts/harness/check-client-contracts.sh` 的同步来源。

## 接口清单

- `INTERFACE: com.aotemiao.artemis.system.client.api.UserValidateService`
- `METHOD: Optional<Long> validate(ValidateCredentialsRequest request)`

## DTO 清单

- `DTO: com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest(String username, String password)`

## 契约说明

- `UserValidateService`
  供 `artemis-auth` 等内部调用方通过 Dubbo 校验用户名与密码，成功时返回 `userId`。
- `ValidateCredentialsRequest`
  作为 Dubbo 契约请求体，与系统服务内部 REST 校验接口保持字段对齐。
