package com.aotemiao.artemis.system.domain.model;

import java.io.Serializable;

/** A single option under a lookup type (e.g. value=1, label=Male). */
public class LookupItem implements Serializable {

    private Long id;
    private Long lookupTypeId;
    private String value;
    private String label;
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
