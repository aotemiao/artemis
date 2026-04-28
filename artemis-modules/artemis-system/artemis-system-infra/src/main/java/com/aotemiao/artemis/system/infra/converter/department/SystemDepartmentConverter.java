package com.aotemiao.artemis.system.infra.converter.department;

import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.infra.dataobject.department.SystemDepartmentDO;

/** 系统部门转换器。 */
public final class SystemDepartmentConverter {

    private SystemDepartmentConverter() {}

    public static SystemDepartmentDO toDO(SystemDepartment systemDepartment) {
        SystemDepartmentDO d = new SystemDepartmentDO();
        d.setId(systemDepartment.getId());
        d.setParentId(systemDepartment.getParentId());
        d.setAncestors(systemDepartment.getAncestors());
        d.setDeptName(systemDepartment.getDeptName());
        d.setDeptCategory(systemDepartment.getDeptCategory());
        d.setSortOrder(systemDepartment.getSortOrder());
        d.setLeaderUserId(systemDepartment.getLeaderUserId());
        d.setPhone(systemDepartment.getPhone());
        d.setEmail(systemDepartment.getEmail());
        d.setStatus(systemDepartment.getStatus());
        d.setRemarks(systemDepartment.getRemarks());
        return d;
    }

    public static SystemDepartment toDomain(SystemDepartmentDO d) {
        SystemDepartment systemDepartment = new SystemDepartment();
        systemDepartment.setId(d.getId());
        systemDepartment.setParentId(d.getParentId());
        systemDepartment.setAncestors(d.getAncestors());
        systemDepartment.setDeptName(d.getDeptName());
        systemDepartment.setDeptCategory(d.getDeptCategory());
        systemDepartment.setSortOrder(d.getSortOrder());
        systemDepartment.setLeaderUserId(d.getLeaderUserId());
        systemDepartment.setPhone(d.getPhone());
        systemDepartment.setEmail(d.getEmail());
        systemDepartment.setStatus(d.getStatus());
        systemDepartment.setRemarks(d.getRemarks());
        return systemDepartment;
    }
}
