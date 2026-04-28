# 岗位 API

本文件描述 `artemis-system` 对外提供的岗位管理接口，并作为 `scripts/harness/check-api-doc-sync.sh` 的同步来源。

## 路由清单

- `ROUTE: POST /api/posts`
- `ROUTE: PUT /api/posts/{id}`
- `ROUTE: DELETE /api/posts/{id}`
- `ROUTE: GET /api/posts/{id}`
- `ROUTE: GET /api/posts`
- `ROUTE: GET /api/posts/select`
- `ROUTE: GET /api/posts/departments/tree`

## 说明

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/posts` | 新增岗位，请求体包含所属部门、岗位编码、类别、名称、排序、状态和备注 |
| `PUT` | `/api/posts/{id}` | 更新指定岗位 |
| `DELETE` | `/api/posts/{id}` | 逻辑删除指定岗位；已分配给用户的岗位不允许删除 |
| `GET` | `/api/posts/{id}` | 按 ID 查询岗位 |
| `GET` | `/api/posts` | 分页查询岗位，使用 `page`、`size` 参数 |
| `GET` | `/api/posts/select` | 查询岗位下拉列表 |
| `GET` | `/api/posts/departments/tree` | 查询岗位表单使用的部门树 |

导出能力后置到导入导出专项；首批补齐基础 CRUD、唯一性约束、下拉选择和部门树依赖。
