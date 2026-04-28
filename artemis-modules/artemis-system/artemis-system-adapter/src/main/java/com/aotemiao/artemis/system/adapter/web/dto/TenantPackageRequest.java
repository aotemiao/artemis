package com.aotemiao.artemis.system.adapter.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** 租户套餐请求。 */
public record TenantPackageRequest(
        @NotBlank String packageName, Boolean menuCheckStrictly, Boolean enabled, String remarks, List<Long> menuIds) {

    public TenantPackageRequest {
        menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }

    @Override
    public List<Long> menuIds() {
        return List.copyOf(menuIds);
    }
}
