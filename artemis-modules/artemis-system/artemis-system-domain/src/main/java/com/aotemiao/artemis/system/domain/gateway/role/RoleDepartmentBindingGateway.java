package com.aotemiao.artemis.system.domain.gateway.role;

import java.util.List;

/** 角色与部门数据权限绑定 Gateway。 */
public interface RoleDepartmentBindingGateway {

    List<Long> findDepartmentIdsByRoleId(Long roleId);

    void replaceDepartments(Long roleId, List<Long> departmentIds);
}
