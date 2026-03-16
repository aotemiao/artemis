package com.aotemiao.artemis.framework.jdbc.base;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仅需 audit 字段（createTime、updateTime、createBy、updateBy）的 DO 基类。
 * 子类需使用 @Table 映射。若需 audit + logical delete，请用 AuditAndSoftDeleteBase。
 */
public abstract class AuditFieldsBase implements Auditable, Serializable {

    @CreatedDate
    @Column("create_time")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column("update_time")
    private LocalDateTime updateTime;

    @CreatedBy
    @Column("create_by")
    private String createBy;

    @LastModifiedBy
    @Column("update_by")
    private String updateBy;

    @Override
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    @Override
    public String getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
}
