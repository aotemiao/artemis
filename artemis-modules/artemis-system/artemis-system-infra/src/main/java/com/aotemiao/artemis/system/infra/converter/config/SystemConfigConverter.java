package com.aotemiao.artemis.system.infra.converter.config;

import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
import com.aotemiao.artemis.system.infra.dataobject.config.SystemConfigDO;

/** 系统参数配置转换器。 */
public final class SystemConfigConverter {

    private SystemConfigConverter() {}

    public static SystemConfigDO toDO(SystemConfig systemConfig) {
        SystemConfigDO d = new SystemConfigDO();
        d.setId(systemConfig.getId());
        d.setConfigName(systemConfig.getConfigName());
        d.setConfigKey(systemConfig.getConfigKey());
        d.setConfigValue(systemConfig.getConfigValue());
        d.setSystemBuiltIn(systemConfig.isSystemBuiltIn());
        d.setRemarks(systemConfig.getRemarks());
        return d;
    }

    public static SystemConfig toDomain(SystemConfigDO d) {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setId(d.getId());
        systemConfig.setConfigName(d.getConfigName());
        systemConfig.setConfigKey(d.getConfigKey());
        systemConfig.setConfigValue(d.getConfigValue());
        systemConfig.setSystemBuiltIn(d.isSystemBuiltIn());
        systemConfig.setRemarks(d.getRemarks());
        return systemConfig;
    }
}
