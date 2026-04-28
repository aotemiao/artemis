package com.aotemiao.artemis.system.infra.dataobject.client;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("system_clients")
public class SystemClientDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("client_id")
    private String clientId;

    @Column("client_key")
    private String clientKey;

    @Column("client_secret")
    private String clientSecret;

    @Column("grant_types")
    private String grantTypes;

    @Column("device_type")
    private String deviceType;

    @Column("active_timeout_seconds")
    private Long activeTimeoutSeconds;

    @Column("fixed_timeout_seconds")
    private Long fixedTimeoutSeconds;

    @Column("status")
    private String status;

    @Column("remarks")
    private String remarks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(String grantTypes) {
        this.grantTypes = grantTypes;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Long getActiveTimeoutSeconds() {
        return activeTimeoutSeconds;
    }

    public void setActiveTimeoutSeconds(Long activeTimeoutSeconds) {
        this.activeTimeoutSeconds = activeTimeoutSeconds;
    }

    public Long getFixedTimeoutSeconds() {
        return fixedTimeoutSeconds;
    }

    public void setFixedTimeoutSeconds(Long fixedTimeoutSeconds) {
        this.fixedTimeoutSeconds = fixedTimeoutSeconds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
