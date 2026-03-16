-- H2-compatible schema for lookup integration tests (BIGSERIAL -> IDENTITY)
-- 表名/列名加双引号，与 Spring Data JDBC 生成的 SQL 一致，避免 H2 MODE=PostgreSQL 下大小写不一致
CREATE TABLE IF NOT EXISTS "lookup_types" (
    "id"          BIGINT AUTO_INCREMENT PRIMARY KEY,
    "code"        VARCHAR(64) NOT NULL,
    "name"        VARCHAR(128),
    "description" VARCHAR(512),
    "create_time" TIMESTAMP,
    "update_time" TIMESTAMP,
    "create_by"   VARCHAR(64),
    "update_by"   VARCHAR(64),
    "deleted"     INT DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS "uk_lookup_types_code" ON "lookup_types" ("code");

-- value 为 H2 保留字，需加双引号
CREATE TABLE IF NOT EXISTS "lookup_items" (
    "id"                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    "lookup_type_id"      BIGINT NOT NULL,
    "lookup_types_key"    INT NOT NULL,
    "value"               VARCHAR(128) NOT NULL,
    "label"               VARCHAR(256),
    "sort_order"          INT DEFAULT 0,
    "create_time"     TIMESTAMP,
    "update_time"     TIMESTAMP,
    "create_by"       VARCHAR(64),
    "update_by"       VARCHAR(64),
    "deleted"         INT DEFAULT 0,
    CONSTRAINT "fk_lookup_items_type" FOREIGN KEY ("lookup_type_id") REFERENCES "lookup_types" ("id")
);
CREATE UNIQUE INDEX IF NOT EXISTS "uk_lookup_items_type_value" ON "lookup_items" ("lookup_type_id", "value");
