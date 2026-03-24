package com.aotemiao.artemis.system.infra.converter;

import com.aotemiao.artemis.system.domain.model.SystemUser;
import com.aotemiao.artemis.system.infra.dataobject.SystemUserDO;

public final class SystemUserConverter {

    private SystemUserConverter() {}

    public static SystemUser toDomain(SystemUserDO systemUserDO) {
        if (systemUserDO == null) {
            return null;
        }
        SystemUser systemUser = new SystemUser();
        systemUser.setId(systemUserDO.getId());
        systemUser.setUsername(systemUserDO.getUsername());
        systemUser.setDisplayName(systemUserDO.getDisplayName());
        systemUser.setPassword(systemUserDO.getPassword());
        systemUser.setEnabled(systemUserDO.isEnabled());
        return systemUser;
    }

    public static SystemUserDO toDO(SystemUser systemUser) {
        if (systemUser == null) {
            return null;
        }
        SystemUserDO systemUserDO = new SystemUserDO();
        systemUserDO.setId(systemUser.getId());
        systemUserDO.setUsername(systemUser.getUsername());
        systemUserDO.setDisplayName(systemUser.getDisplayName());
        systemUserDO.setPassword(systemUser.getPassword());
        systemUserDO.setEnabled(systemUser.isEnabled());
        return systemUserDO;
    }
}
