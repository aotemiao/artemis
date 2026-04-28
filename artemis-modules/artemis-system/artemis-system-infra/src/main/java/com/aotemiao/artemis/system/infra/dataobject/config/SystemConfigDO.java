package com.aotemiao.artemis.system.infra.dataobject.config;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("system_configs")
public class SystemConfigDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("config_name")
    private String configName;

    @Column("config_key")
    private String configKey;

    @Column("config_value")
    private String configValue;

    @Column("system_built_in")
    private boolean systemBuiltIn;

    @Column("remarks")
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
