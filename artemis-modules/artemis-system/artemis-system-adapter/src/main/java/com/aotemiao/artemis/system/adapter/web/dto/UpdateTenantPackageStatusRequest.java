package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotNull;

/** 租户套餐状态修改请求。 */
public record UpdateTenantPackageStatusRequest(@NotNull Boolean enabled) {}
