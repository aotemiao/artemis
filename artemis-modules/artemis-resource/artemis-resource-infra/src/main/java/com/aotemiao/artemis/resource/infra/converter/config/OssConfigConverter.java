package com.aotemiao.artemis.resource.infra.converter.config;

import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import com.aotemiao.artemis.resource.infra.dataobject.config.OssConfigDO;

public final class OssConfigConverter {

    private OssConfigConverter() {}

    public static OssConfig toDomain(OssConfigDO source) {
        OssConfig target = new OssConfig();
        target.setId(source.getId());
        target.setConfigKey(source.getConfigKey());
        target.setAccessKey(source.getAccessKey());
        target.setSecretKey(source.getSecretKey());
        target.setBucket(source.getBucket());
        target.setPrefix(source.getPrefix());
        target.setEndpoint(source.getEndpoint());
        target.setCustomDomain(source.getCustomDomain());
        target.setHttpsEnabled(source.getHttpsEnabled());
        target.setRegion(source.getRegion());
        target.setAccessPolicy(source.getAccessPolicy());
        target.setStatus(source.getStatus());
        target.setDefaultFlag(source.getDefaultFlag());
        target.setBuiltIn(source.getBuiltIn());
        target.setProvider(source.getProvider());
        target.setExtJson(source.getExtJson());
        return target;
    }

    public static OssConfigDO toDO(OssConfig source) {
        OssConfigDO target = new OssConfigDO();
        target.setId(source.getId());
        target.setConfigKey(source.getConfigKey());
        target.setAccessKey(source.getAccessKey());
        target.setSecretKey(source.getSecretKey());
        target.setBucket(source.getBucket());
        target.setPrefix(source.getPrefix());
        target.setEndpoint(source.getEndpoint());
        target.setCustomDomain(source.getCustomDomain());
        target.setHttpsEnabled(source.getHttpsEnabled());
        target.setRegion(source.getRegion());
        target.setAccessPolicy(source.getAccessPolicy());
        target.setStatus(source.getStatus());
        target.setDefaultFlag(source.getDefaultFlag());
        target.setBuiltIn(source.getBuiltIn());
        target.setProvider(source.getProvider());
        target.setExtJson(source.getExtJson());
        return target;
    }
}
