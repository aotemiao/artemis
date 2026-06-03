package com.aotemiao.artemis.system.adapter.web.dto.config;

import jakarta.validation.constraints.NotBlank;

/** 系统参数创建或更新请求。 */
public record SystemConfigRequest(
        @NotBlank String configName,
        @NotBlank String configKey,
        @NotBlank String configValue,
        Boolean systemBuiltIn,
        String remarks) {}
