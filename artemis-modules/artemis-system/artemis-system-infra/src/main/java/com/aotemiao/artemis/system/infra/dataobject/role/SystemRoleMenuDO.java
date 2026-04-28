package com.aotemiao.artemis.system.infra.dataobject.role;

import com.aotemiao.artemis.framework.jdbc.base.AuditFieldsBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("system_role_menus")
public class SystemRoleMenuDO extends AuditFieldsBase {

    @Id
    @Column("id")
    private Long id;

    @Column("role_id")
    private Long roleId;

    @Column("menu_id")
    private Long menuId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
