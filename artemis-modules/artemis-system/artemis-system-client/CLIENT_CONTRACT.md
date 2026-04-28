# Artemis System Client 契约

本文件描述 `artemis-system-client` 当前对外发布的内部契约，并作为 `scripts/harness/check-client-contracts.sh` 的同步来源。

## 接口清单

- `INTERFACE: com.aotemiao.artemis.system.client.api.UserValidateService`
- `METHOD: Optional<Long> validate(ValidateCredentialsRequest request)`
- `INTERFACE: com.aotemiao.artemis.system.client.api.UserRegisterService`
- `METHOD: Long register(RegisterUserRequest request)`
- `INTERFACE: com.aotemiao.artemis.system.client.api.SystemClientValidateService`
- `METHOD: boolean validate(ValidateClientRequest request)`
- `INTERFACE: com.aotemiao.artemis.system.client.api.LoginInfoRecordService`
- `METHOD: void record(RecordLoginInfoRequest request)`
- `INTERFACE: com.aotemiao.artemis.system.client.api.UserAuthorizationService`
- `METHOD: Optional<UserAuthorizationSnapshotDTO> getByUserId(Long userId)`

## DTO 清单

- `DTO: com.aotemiao.artemis.system.client.dto.ValidateCredentialsRequest(String clientId, String grantType, String username, String password)`
- `DTO: com.aotemiao.artemis.system.client.dto.RegisterUserRequest(String tenantId, String clientId, String grantType, String username, String password, String userType)`
- `DTO: com.aotemiao.artemis.system.client.dto.ValidateClientRequest(String clientId, String grantType)`
- `DTO: com.aotemiao.artemis.system.client.dto.RecordLoginInfoRequest(String tenantId, String username, String clientId, String deviceType, String ipaddr, String loginLocation, String browser, String os, String status, String msg)`
- `DTO: com.aotemiao.artemis.system.client.dto.UserAuthorizationSnapshotDTO(Long userId, String username, String displayName, List<String> roleKeys, List<String> permissionCodes)`

## 契约说明

- `UserValidateService`
  供 `artemis-auth` 等内部调用方通过 Dubbo 校验客户端、授权类型、用户名与密码，成功时返回 `userId`。
- `UserRegisterService`
  供 `artemis-auth` 等内部调用方注册系统用户；系统服务负责注册开关、用户类型和用户名唯一性校验。
- `SystemClientValidateService`
  供 `artemis-auth` 等内部调用方在登录前校验客户端存在、状态正常且支持指定授权类型。
- `LoginInfoRecordService`
  供 `artemis-auth` 等内部调用方记录登录成功、失败与登出访问日志。
- `ValidateCredentialsRequest`
  作为 Dubbo 契约请求体，与系统服务内部 REST 校验接口保持字段对齐。
- `RegisterUserRequest`
  作为用户注册请求体，包含租户、客户端、授权类型、用户名、密码和用户类型。
- `ValidateClientRequest`
  作为客户端授权校验请求体，与系统服务内部 REST 客户端校验接口保持字段对齐。
- `RecordLoginInfoRequest`
  作为登录访问日志请求体，记录租户、用户账号、客户端、设备、IP、地点、浏览器、操作系统、状态和消息。
- `UserAuthorizationService`
  供 `artemis-auth` 等内部调用方按 `userId` 查询最小授权快照，成功时返回用户基础信息、启用中的 `roleKeys` 与 `permissionCodes`。
- `UserAuthorizationSnapshotDTO`
  作为内部授权快照结果体，与系统服务内部 REST 授权查询接口保持字段对齐。
