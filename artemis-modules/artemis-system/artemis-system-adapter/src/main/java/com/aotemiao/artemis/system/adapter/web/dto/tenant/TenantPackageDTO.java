package com.aotemiao.artemis.system.adapter.web.dto.tenant;

import java.io.Serializable;
import java.util.List;

/** 租户套餐响应 DTO。 */
public record TenantPackageDTO(
        Long id, String packageName, boolean menuCheckStrictly, boolean enabled, String remarks, List<Long> menuIds)
        implements Serializable {

    public TenantPackageDTO {
        menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }

    @Override
    public List<Long> menuIds() {
        return List.copyOf(menuIds);
    }
}
