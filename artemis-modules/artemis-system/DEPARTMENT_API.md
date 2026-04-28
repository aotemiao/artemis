# 部门 API

本文件描述 `artemis-system` 对外提供的部门管理接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/departments`
- `ROUTE: PUT /api/departments/{id}`
- `ROUTE: DELETE /api/departments/{id}`
- `ROUTE: GET /api/departments/{id}`
- `ROUTE: GET /api/departments`
- `ROUTE: GET /api/departments/tree`
- `ROUTE: GET /api/departments/tree/exclude/{id}`
- `ROUTE: GET /api/departments/select`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/departments` | 新增部门，请求体包含父部门、名称、类别、排序、负责人、电话、邮箱、状态、备注 |
| `PUT` | `/api/departments/{id}` | 更新指定部门；移动父级时同步子部门祖级列表 |
| `DELETE` | `/api/departments/{id}` | 逻辑删除指定部门；存在子部门时不允许删除 |
| `GET` | `/api/departments/{id}` | 按 ID 查询部门 |
| `GET` | `/api/departments` | 查询部门平铺列表 |
| `GET` | `/api/departments/tree` | 查询部门树 |
| `GET` | `/api/departments/tree/exclude/{id}` | 查询排除指定节点及子节点后的部门树 |
| `GET` | `/api/departments/select` | 查询部门下拉树 |
