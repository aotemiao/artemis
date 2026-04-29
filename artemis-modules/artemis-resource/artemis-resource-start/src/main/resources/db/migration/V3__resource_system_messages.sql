CREATE TABLE resource_system_messages (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255) NOT NULL,
    content           TEXT NOT NULL,
    sender            VARCHAR(64) NOT NULL,
    recipient_user_id BIGINT NOT NULL,
    broadcast_flag    TINYINT NOT NULL DEFAULT 0,
    read_flag         TINYINT NOT NULL DEFAULT 0,
    read_time         DATETIME,
    ext_json          TEXT,
    create_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by         VARCHAR(64),
    update_by         VARCHAR(64),
    deleted           TINYINT NOT NULL DEFAULT 0,
    KEY idx_resource_system_messages_inbox (recipient_user_id, deleted, id)
);
