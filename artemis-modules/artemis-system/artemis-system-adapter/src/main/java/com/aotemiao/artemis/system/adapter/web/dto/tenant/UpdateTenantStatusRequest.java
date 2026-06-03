package com.aotemiao.artemis.system.adapter.web.dto.tenant;

import jakarta.validation.constraints.NotBlank;

public record UpdateTenantStatusRequest(@NotBlank String status) {}
