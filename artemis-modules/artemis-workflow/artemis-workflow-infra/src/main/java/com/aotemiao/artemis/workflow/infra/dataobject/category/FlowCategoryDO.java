package com.aotemiao.artemis.workflow.infra.dataobject.category;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("workflow_flow_categories")
public class FlowCategoryDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("parent_id")
    private Long parentId;

    @Column("ancestors")
    private String ancestors;

    @Column("category_name")
    private String categoryName;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("remarks")
    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
