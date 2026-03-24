package com.aotemiao.artemis.system.infra.repository;

import com.aotemiao.artemis.system.infra.dataobject.SystemUserRoleDO;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SystemUserRoleRepository extends CrudRepository<SystemUserRoleDO, Long> {

    List<SystemUserRoleDO> findAllByUserIdOrderById(Long userId);
}
