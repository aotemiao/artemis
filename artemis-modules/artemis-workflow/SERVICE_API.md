# Workflow Service API

Status: maintained
Last Reviewed: 2026-03-24
Review Cadence: 90 days

- `ROUTE: GET /api/workflow/ping`
- 用途：验证 `artemis-workflow` 已经完成最小服务装配
- 返回：`serviceCode`、`capability` 与 `message`
- `ROUTE: GET /api/workflow/categories`
- 用途：分页查询流程分类
- 返回：流程分类分页结果
- `ROUTE: GET /api/workflow/categories/{id}`
- 用途：查询流程分类详情
- 返回：流程分类详情
- `ROUTE: POST /api/workflow/categories`
- 用途：创建流程分类
- 请求：`parentId`、`categoryName`、`sortOrder`、`remarks`
- 返回：创建后的流程分类
- `ROUTE: PUT /api/workflow/categories/{id}`
- 用途：修改流程分类
- 请求：`parentId`、`categoryName`、`sortOrder`、`remarks`
- 返回：修改后的流程分类
- `ROUTE: DELETE /api/workflow/categories/{id}`
- 用途：删除流程分类
- 返回：删除结果
- `ROUTE: GET /api/workflow/categories/tree`
- 用途：查询流程分类树
- 返回：流程分类树
- `ROUTE: GET /api/workflow/categories/export`
- 用途：导出流程分类数据载荷
- 返回：流程分类列表
