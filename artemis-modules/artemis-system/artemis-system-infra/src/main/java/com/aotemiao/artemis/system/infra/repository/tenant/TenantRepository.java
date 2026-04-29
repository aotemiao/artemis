package com.aotemiao.artemis.system.infra.repository.tenant;

import com.aotemiao.artemis.system.infra.dataobject.tenant.TenantDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface TenantRepository extends CrudRepository<TenantDO, Long> {

    Optional<TenantDO> findByTenantNoAndDeleted(String tenantNo, Integer deleted);

    Optional<TenantDO> findByCompanyNameAndDeleted(String companyName, Integer deleted);

    Page<TenantDO> findAllByDeletedOrderByIdDesc(Integer deleted, Pageable pageable);

    List<TenantDO> findAllByDeletedAndStatusOrderByIdDesc(Integer deleted, String status);

    boolean existsByPackageIdAndDeleted(Long packageId, Integer deleted);
}
