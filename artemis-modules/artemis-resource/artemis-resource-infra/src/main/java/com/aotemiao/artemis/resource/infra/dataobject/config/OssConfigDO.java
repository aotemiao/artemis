package com.aotemiao.artemis.resource.infra.dataobject.config;

import com.aotemiao.artemis.framework.jdbc.base.AuditAndSoftDeleteBase;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("resource_oss_configs")
public class OssConfigDO extends AuditAndSoftDeleteBase {

    @Id
    @Column("id")
    private Long id;

    @Column("config_key")
    private String configKey;

    @Column("access_key")
    private String accessKey;

    @Column("secret_key")
    private String secretKey;

    @Column("bucket")
    private String bucket;

    @Column("prefix")
    private String prefix;

    @Column("endpoint")
    private String endpoint;

    @Column("custom_domain")
    private String customDomain;

    @Column("https_enabled")
    private Boolean httpsEnabled;

    @Column("region")
    private String region;

    @Column("access_policy")
    private String accessPolicy;

    @Column("status")
    private Integer status;

    @Column("default_flag")
    private Integer defaultFlag;

    @Column("built_in")
    private Integer builtIn;

    @Column("provider")
    private String provider;

    @Column("ext_json")
    private String extJson;

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
