package com.aotemiao.artemis.resource.domain.model.config;

import java.io.Serializable;

/** 对象存储配置。 */
public class OssConfig implements Serializable {

    private Long id;
    private String configKey;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String prefix;
    private String endpoint;
    private String customDomain;
    private Boolean httpsEnabled;
    private String region;
    private String accessPolicy;
    private Integer status;
    private Integer defaultFlag;
    private Integer builtIn;
    private String provider;
    private String extJson;

    public boolean isBuiltIn() {
        return Integer.valueOf(1).equals(builtIn);
    }

    public boolean isEnabled() {
        return Integer.valueOf(1).equals(status);
    }

    public boolean isDefault() {
        return Integer.valueOf(1).equals(defaultFlag);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public Boolean getHttpsEnabled() {
        return httpsEnabled;
    }

    public void setHttpsEnabled(Boolean httpsEnabled) {
        this.httpsEnabled = httpsEnabled;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessPolicy() {
        return accessPolicy;
    }

    public void setAccessPolicy(String accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(Integer defaultFlag) {
        this.defaultFlag = defaultFlag;
    }

    public Integer getBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(Integer builtIn) {
        this.builtIn = builtIn;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getExtJson() {
        return extJson;
    }

    public void setExtJson(String extJson) {
        this.extJson = extJson;
    }
}
