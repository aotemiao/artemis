package com.aotemiao.artemis.framework.jdbc.base;

import java.time.LocalDateTime;

/**
 * 具有 audit 字段（createTime、updateTime、createBy、updateBy）的实体的 marker 与 contract。 由 AuditEntityCallback
 * 在 insert/update 前填充这些字段。
 */
public interface Auditable {

    LocalDateTime getCreateTime();

    void setCreateTime(LocalDateTime createTime);

    LocalDateTime getUpdateTime();

    void setUpdateTime(LocalDateTime updateTime);

    String getCreateBy();

    void setCreateBy(String createBy);

    String getUpdateBy();

    void setUpdateBy(String updateBy);
}
