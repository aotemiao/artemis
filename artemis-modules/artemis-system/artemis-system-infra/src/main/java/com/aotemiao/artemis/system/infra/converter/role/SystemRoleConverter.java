package com.aotemiao.artemis.system.infra.converter.role;

import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import com.aotemiao.artemis.system.infra.dataobject.role.SystemRoleDO;

/** 系统角色 Domain / DO 转换器。 */
public final class SystemRoleConverter {

    private SystemRoleConverter() {}

    public static SystemRoleDO toDO(SystemRole systemRole) {
        if (systemRole == null) {
            return null;
        }
        SystemRoleDO systemRoleDO = new SystemRoleDO();
        systemRoleDO.setId(systemRole.getId());
        systemRoleDO.setRoleKey(systemRole.getRoleKey());
        systemRoleDO.setRoleName(systemRole.getRoleName());
        systemRoleDO.setDataScope(systemRole.getDataScope());
        systemRoleDO.setEnabled(systemRole.isEnabled());
        return systemRoleDO;
    }

    public static SystemRole toDomain(SystemRoleDO systemRoleDO) {
        if (systemRoleDO == null) {
            return null;
        }
        SystemRole systemRole = new SystemRole();
        systemRole.setId(systemRoleDO.getId());
        systemRole.setRoleKey(systemRoleDO.getRoleKey());
        systemRole.setRoleName(systemRoleDO.getRoleName());
        systemRole.setDataScope(systemRoleDO.getDataScope());
        systemRole.setEnabled(systemRoleDO.isEnabled());
        return systemRole;
    }
}
