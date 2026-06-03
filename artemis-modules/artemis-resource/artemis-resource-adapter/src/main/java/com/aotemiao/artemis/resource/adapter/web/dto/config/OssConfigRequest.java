package com.aotemiao.artemis.resource.adapter.web.dto.config;

public record OssConfigRequest(
        String configKey,
        String accessKey,
        String secretKey,
        String bucket,
        String prefix,
        String endpoint,
        String customDomain,
        Boolean httpsEnabled,
        String region,
        String accessPolicy,
        Integer status,
        Integer defaultFlag,
        Integer builtIn,
        String provider,
        String extJson) {}
