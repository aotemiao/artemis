package com.aotemiao.artemis.system.adapter.web.dto.config;

import jakarta.validation.constraints.NotBlank;

/** 更新系统参数值请求。 */
public record UpdateSystemConfigValueRequest(@NotBlank String configValue) {}
