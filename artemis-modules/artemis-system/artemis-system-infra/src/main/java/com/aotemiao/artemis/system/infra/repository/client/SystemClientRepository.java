package com.aotemiao.artemis.system.infra.repository.client;

import com.aotemiao.artemis.system.infra.dataobject.client.SystemClientDO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SystemClientRepository extends CrudRepository<SystemClientDO, Long> {

    Optional<SystemClientDO> findByClientIdAndDeleted(String clientId, Integer deleted);

    Optional<SystemClientDO> findByClientKeyAndDeleted(String clientKey, Integer deleted);

    Page<SystemClientDO> findAllByDeletedOrderById(Integer deleted, Pageable pageable);
}
