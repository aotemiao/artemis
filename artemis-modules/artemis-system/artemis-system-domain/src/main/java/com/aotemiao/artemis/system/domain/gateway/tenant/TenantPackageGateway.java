package com.aotemiao.artemis.system.domain.gateway.tenant;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import java.util.List;
import java.util.Optional;

/** 租户套餐 Gateway。 */
public interface TenantPackageGateway {

    TenantPackage save(TenantPackage tenantPackage);

    Optional<TenantPackage> findById(Long id);

    PageResult<TenantPackage> findPage(PageRequest pageRequest);

    List<TenantPackage> findEnabled();

    boolean existsByPackageName(String packageName, Long excludeId);

    boolean isUsedByTenant(Long packageId);

    void deleteById(Long id);
}
