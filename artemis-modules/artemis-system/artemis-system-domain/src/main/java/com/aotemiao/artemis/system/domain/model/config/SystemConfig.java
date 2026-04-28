package com.aotemiao.artemis.system.domain.model.config;

import java.io.Serializable;

/** 系统参数配置。 */
public class SystemConfig implements Serializable {

    private Long id;
    private String configName;
    private String configKey;
    private String configValue;
    private boolean systemBuiltIn;
    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public boolean isSystemBuiltIn() {
        return systemBuiltIn;
    }

    public void setSystemBuiltIn(boolean systemBuiltIn) {
        this.systemBuiltIn = systemBuiltIn;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
