# 登录日志 API

## 路由

- `ROUTE: GET /api/login-infos`
- `ROUTE: GET /api/login-infos/{id}`
- `ROUTE: DELETE /api/login-infos`
- `ROUTE: POST /api/login-infos/clear`

## 说明

- 分页查询按登录时间倒序返回访问记录。
- 登录日志包含租户、用户账号、客户端、设备类型、IP、地点、浏览器、操作系统、状态、消息和登录时间。
- 删除和清空采用逻辑删除。
- 导出和账户解锁后置到导入导出/锁定策略专项，不阻塞当前最小闭环。
