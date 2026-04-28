# Oper Log API

## Routes

- `ROUTE: GET /api/oper-logs`
- `ROUTE: GET /api/oper-logs/{id}`
- `ROUTE: DELETE /api/oper-logs`
- `ROUTE: POST /api/oper-logs/clear`

## Contracts

- `GET /api/oper-logs`
  分页查询后台操作日志，默认按操作时间倒序返回。
- `GET /api/oper-logs/{id}`
  查询后台操作日志详情。
- `DELETE /api/oper-logs`
  请求体为 `ids`，批量逻辑删除后台操作日志。
- `POST /api/oper-logs/clear`
  清空后台操作日志。

## Logging

后台写操作可通过 `@OperLogRecord` 自动记录操作日志。当前记录字段包含模块标题、业务类型、方法、请求方式、操作类别、操作人、部门、URL、IP、地点、请求参数、返回结果、状态、错误信息、耗时和操作时间。
