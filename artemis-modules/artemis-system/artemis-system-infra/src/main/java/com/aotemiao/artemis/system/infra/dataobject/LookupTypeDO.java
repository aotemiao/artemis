package com.aotemiao.artemis.system.infra.dataobject;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table("lookup_types")
public class LookupTypeDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("code")
    private String code;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @MappedCollection(idColumn = "lookup_type_id", keyColumn = "lookup_types_key")
    private List<LookupItemDO> items = new ArrayList<>();

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

    public List<LookupItemDO> getItems() {
        return items;
    }

    public void setItems(List<LookupItemDO> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
