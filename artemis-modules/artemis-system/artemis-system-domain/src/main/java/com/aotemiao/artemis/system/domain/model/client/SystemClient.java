package com.aotemiao.artemis.system.domain.model.client;

import java.io.Serializable;
import java.util.Arrays;

/** 系统客户端授权配置。 */
public class SystemClient implements Serializable {

    private Long id;
    private String clientId;
    private String clientKey;
    private String clientSecret;
    private String grantTypes;
    private String deviceType;
    private Long activeTimeoutSeconds;
    private Long fixedTimeoutSeconds;
    private String status;
    private String remarks;

    public boolean supportsGrantType(String grantType) {
        if (grantType == null || grantTypes == null) {
            return false;
        }
        return Arrays.stream(grantTypes.split(",")).map(String::trim).anyMatch(item -> item.equals(grantType));
    }

    public boolean isNormal() {
        return "NORMAL".equals(status);
    }

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
