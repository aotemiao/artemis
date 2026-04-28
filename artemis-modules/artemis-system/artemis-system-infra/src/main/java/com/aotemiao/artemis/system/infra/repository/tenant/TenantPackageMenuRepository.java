package com.aotemiao.artemis.system.infra.repository.tenant;

import com.aotemiao.artemis.system.infra.dataobject.tenant.TenantPackageMenuDO;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface TenantPackageMenuRepository extends CrudRepository<TenantPackageMenuDO, Long> {

    List<TenantPackageMenuDO> findAllByPackageId(Long packageId);

    void deleteAllByPackageId(Long packageId);
}
