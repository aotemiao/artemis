# 租户 API

本文件描述 `artemis-system` 对外提供的租户管理接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/tenants`
- `ROUTE: PUT /api/tenants/{id}`
- `ROUTE: PUT /api/tenants/{id}/status`
- `ROUTE: DELETE /api/tenants/{id}`
- `ROUTE: GET /api/tenants/{id}`
- `ROUTE: GET /api/tenants`
- `ROUTE: GET /api/tenants/select`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/tenants` | 新增租户，请求体包含企业名称、联系人、电话、社会信用代码、地址、域名、简介、套餐、有效期、用户数量额度和备注 |
| `PUT` | `/api/tenants/{id}` | 更新指定租户；不允许直接修改租户编号和套餐 |
| `PUT` | `/api/tenants/{id}/status` | 启用或停用指定租户 |
| `DELETE` | `/api/tenants/{id}` | 逻辑删除指定租户；默认管理租户不允许操作 |
| `GET` | `/api/tenants/{id}` | 按 ID 查询租户 |
| `GET` | `/api/tenants` | 分页查询租户，使用 `page`、`size` 参数 |
| `GET` | `/api/tenants/select` | 查询启用状态的租户下拉列表 |

租户初始化骨架已接入租户管理员角色、根部门、首个系统用户和角色绑定；字典、参数和工作流同步后置到后续专项。
