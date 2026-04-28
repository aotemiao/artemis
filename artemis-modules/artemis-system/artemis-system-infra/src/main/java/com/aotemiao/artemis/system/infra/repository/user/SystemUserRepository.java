package com.aotemiao.artemis.system.infra.repository.user;

import com.aotemiao.artemis.system.infra.dataobject.user.SystemUserDO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SystemUserRepository extends CrudRepository<SystemUserDO, Long> {

    Page<SystemUserDO> findAllByDeletedOrderById(Integer deleted, Pageable pageable);

    Optional<SystemUserDO> findByUsernameAndDeleted(String username, Integer deleted);
}
