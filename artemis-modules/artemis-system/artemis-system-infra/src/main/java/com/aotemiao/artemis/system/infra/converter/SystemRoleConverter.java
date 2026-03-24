package com.aotemiao.artemis.system.infra.converter;

import com.aotemiao.artemis.system.domain.model.SystemRole;
import com.aotemiao.artemis.system.infra.dataobject.SystemRoleDO;

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
        systemRole.setEnabled(systemRoleDO.isEnabled());
        return systemRole;
    }
}
