package com.aotemiao.artemis.system.infra.dataobject;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("lookup_items")
public class LookupItemDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("lookup_type_id")
    private Long lookupTypeId;

    @Column("value")
    private String value;
    @Column("label")
    private String label;

    @Column("sort_order")
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLookupTypeId() {
        return lookupTypeId;
    }

    public void setLookupTypeId(Long lookupTypeId) {
        this.lookupTypeId = lookupTypeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
