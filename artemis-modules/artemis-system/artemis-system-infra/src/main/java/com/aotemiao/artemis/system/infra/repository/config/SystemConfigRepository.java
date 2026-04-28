package com.aotemiao.artemis.system.infra.repository.config;

import com.aotemiao.artemis.system.infra.dataobject.config.SystemConfigDO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SystemConfigRepository extends CrudRepository<SystemConfigDO, Long> {

    Page<SystemConfigDO> findAllByDeletedOrderById(Integer deleted, Pageable pageable);

    Optional<SystemConfigDO> findByConfigKeyAndDeleted(String configKey, Integer deleted);
}
