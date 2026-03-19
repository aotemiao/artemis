package com.aotemiao.artemis.framework.jdbc.base;

import org.springframework.data.relational.core.mapping.Column;

/** 同时需要 audit 字段与 logical delete 的 DO 基类。 子类需使用 @Table 映射；查询时用 deleted = 0 过滤已逻辑删除的行。 */
public abstract class AuditAndSoftDeleteBase extends AuditFieldsBase implements SoftDeletable {

    @Column("deleted")
    private Integer deleted = 0;

    @Override
    public Integer getDeleted() {
        return deleted;
    }

    @Override
    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
