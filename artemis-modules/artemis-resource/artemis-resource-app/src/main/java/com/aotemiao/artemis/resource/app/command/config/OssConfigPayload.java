package com.aotemiao.artemis.resource.app.command.config;

/** OSS 配置写入载荷。 */
public record OssConfigPayload(
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
