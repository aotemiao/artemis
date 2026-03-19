package com.aotemiao.artemis.system.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 字典类型聚合根（如 user_gender、order_status）。 包含 LookupItem 选项列表。 */
public class LookupType implements Serializable {

    private Long id;
    private String code;
    private String name;
    private String description;
    private List<LookupItem> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<LookupItem> getItems() {
        return items;
    }

    public void setItems(List<LookupItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
