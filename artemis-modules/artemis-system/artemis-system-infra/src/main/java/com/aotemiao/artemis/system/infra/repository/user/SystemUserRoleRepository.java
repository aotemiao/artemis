package com.aotemiao.artemis.system.infra.repository.user;

import com.aotemiao.artemis.system.infra.dataobject.user.SystemUserRoleDO;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SystemUserRoleRepository extends CrudRepository<SystemUserRoleDO, Long> {

    List<SystemUserRoleDO> findAllByUserIdOrderById(Long userId);
}
