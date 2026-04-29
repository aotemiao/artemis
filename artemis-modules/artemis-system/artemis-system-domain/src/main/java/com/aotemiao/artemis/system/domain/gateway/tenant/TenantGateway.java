package com.aotemiao.artemis.system.domain.gateway.tenant;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import java.util.List;
import java.util.Optional;

/** 租户 Gateway。 */
public interface TenantGateway {

    Tenant save(Tenant tenant);

    Optional<Tenant> findById(Long id);

    Optional<Tenant> findByTenantNo(String tenantNo);

    Optional<Tenant> findByCompanyName(String companyName);

    PageResult<Tenant> findPage(PageRequest pageRequest);

    List<Tenant> findEnabled();

    boolean existsByTenantNo(String tenantNo, Long excludeId);

    boolean existsByCompanyName(String companyName, Long excludeId);

    void deleteById(Long id);
}
