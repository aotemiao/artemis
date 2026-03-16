# Lookup API

Base path: `/api/lookup-types`

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/lookup-types | Create lookup type (body: code, name, description, items[]) |
| PUT | /api/lookup-types/{id} | Update lookup type |
| DELETE | /api/lookup-types/{id} | Logical delete lookup type |
| GET | /api/lookup-types/{id} | Get lookup type by id |
| GET | /api/lookup-types?page=0&size=10 | Page lookup types |
| GET | /api/lookup-types/{typeCode}/items | Get lookup items by type code (e.g. user_gender) |

表结构由 Flyway 在应用启动时自动应用（`db/migration` 下的脚本），无需手动执行 SQL 或 Flyway CLI。
