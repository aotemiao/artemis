package com.aotemiao.artemis.system.infra.repository.tenant;

import com.aotemiao.artemis.system.infra.dataobject.tenant.TenantPackageDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface TenantPackageRepository extends CrudRepository<TenantPackageDO, Long> {

    Page<TenantPackageDO> findAllByDeletedOrderByIdDesc(Integer deleted, Pageable pageable);

    List<TenantPackageDO> findAllByDeletedAndEnabledOrderByIdDesc(Integer deleted, Boolean enabled);

    Optional<TenantPackageDO> findByPackageNameAndDeleted(String packageName, Integer deleted);
}
