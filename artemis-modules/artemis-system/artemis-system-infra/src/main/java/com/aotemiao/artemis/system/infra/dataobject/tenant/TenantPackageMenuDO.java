package com.aotemiao.artemis.system.infra.dataobject.tenant;

import com.aotemiao.artemis.framework.jdbc.base.AuditFieldsBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("system_tenant_package_menus")
public class TenantPackageMenuDO extends AuditFieldsBase {

    @Id
    @Column("id")
    private Long id;

    @Column("package_id")
    private Long packageId;

    @Column("menu_id")
    private Long menuId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
