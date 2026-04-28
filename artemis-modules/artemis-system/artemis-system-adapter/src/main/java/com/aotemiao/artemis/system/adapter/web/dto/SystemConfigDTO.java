package com.aotemiao.artemis.system.adapter.web.dto;

import java.io.Serializable;

/** 系统参数响应 DTO。 */
public record SystemConfigDTO(
        Long id, String configName, String configKey, String configValue, Boolean systemBuiltIn, String remarks)
        implements Serializable {}
