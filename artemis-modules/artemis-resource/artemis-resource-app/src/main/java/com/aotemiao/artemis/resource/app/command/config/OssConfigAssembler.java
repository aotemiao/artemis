package com.aotemiao.artemis.resource.app.command.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;

final class OssConfigAssembler {

    private OssConfigAssembler() {}

    static OssConfig toNewConfig(OssConfigPayload payload) {
        OssConfig config = new OssConfig();
        apply(config, payload);
        return config;
    }

    static void apply(OssConfig config, OssConfigPayload payload) {
        if (payload == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Oss config payload must not be null");
        }
        requireText(payload.configKey(), "Config key must not be blank");
        requireText(payload.accessKey(), "Access key must not be blank");
        requireText(payload.secretKey(), "Secret key must not be blank");
        requireText(payload.bucket(), "Bucket must not be blank");
        requireText(payload.provider(), "Provider must not be blank");
        config.setConfigKey(payload.configKey().strip());
        config.setAccessKey(payload.accessKey().strip());
        config.setSecretKey(payload.secretKey().strip());
        config.setBucket(payload.bucket().strip());
        config.setPrefix(blankToNull(payload.prefix()));
        config.setEndpoint(blankToNull(payload.endpoint()));
        config.setCustomDomain(blankToNull(payload.customDomain()));
        config.setHttpsEnabled(payload.httpsEnabled() == null || payload.httpsEnabled());
        config.setRegion(blankToNull(payload.region()));
        config.setAccessPolicy(defaultText(payload.accessPolicy(), "PRIVATE"));
        config.setStatus(
                payload.status() == null
                        ? Integer.valueOf(1)
                        : normalizeFlag(payload.status(), "Status must be 0 or 1"));
        config.setDefaultFlag(
                payload.defaultFlag() == null
                        ? Integer.valueOf(0)
                        : normalizeFlag(payload.defaultFlag(), "Default flag must be 0 or 1"));
        config.setBuiltIn(
                payload.builtIn() == null
                        ? Integer.valueOf(0)
                        : normalizeFlag(payload.builtIn(), "Built-in flag must be 0 or 1"));
        config.setProvider(payload.provider().strip().toUpperCase());
        config.setExtJson(blankToNull(payload.extJson()));
    }

    static Integer normalizeStatus(Integer status) {
        if (status == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Status must not be null");
        }
        return normalizeFlag(status, "Status must be 0 or 1");
    }

    private static Integer normalizeFlag(Integer value, String message) {
        if (Integer.valueOf(0).equals(value) || Integer.valueOf(1).equals(value)) {
            return value;
        }
        throw new BizException(CommonErrorCode.BAD_REQUEST, message);
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, message);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.strip().toUpperCase();
    }
}
