package com.aotemiao.artemis.system.infra.dataobject.role;

import com.aotemiao.artemis.framework.jdbc.base.AuditFieldsBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("system_role_departments")
public class SystemRoleDepartmentDO extends AuditFieldsBase {

    @Id
    @Column("id")
    private Long id;

    @Column("role_id")
    private Long roleId;

    @Column("department_id")
    private Long departmentId;

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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
