package com.aotemiao.artemis.framework.jdbc.base;

/**
 * 支持 logical delete（deleted 字段）的实体的 marker 与 contract。
 * 查询时用 deleted = 0 过滤已逻辑删除的行。
 */
public interface SoftDeletable {

    Integer getDeleted();

    void setDeleted(Integer deleted);
}
