-- Lookup types (reference data type, e.g. user_gender, order_status)
CREATE TABLE IF NOT EXISTS lookup_types (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(64) NOT NULL,
    name        VARCHAR(128),
    description VARCHAR(512),
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by   VARCHAR(64),
    update_by   VARCHAR(64),
    deleted     INT DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_lookup_types_code ON lookup_types (code) WHERE deleted = 0;

-- Lookup items (options under a type)
CREATE TABLE IF NOT EXISTS lookup_items (
    id              BIGSERIAL PRIMARY KEY,
    lookup_type_id  BIGINT NOT NULL,
    value           VARCHAR(128) NOT NULL,
    label           VARCHAR(256),
    sort_order      INT DEFAULT 0,
    create_time     TIMESTAMP,
    update_time     TIMESTAMP,
    create_by       VARCHAR(64),
    update_by       VARCHAR(64),
    deleted         INT DEFAULT 0,
    CONSTRAINT fk_lookup_items_type FOREIGN KEY (lookup_type_id) REFERENCES lookup_types (id)
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_lookup_items_type_value ON lookup_items (lookup_type_id, value) WHERE deleted = 0;
