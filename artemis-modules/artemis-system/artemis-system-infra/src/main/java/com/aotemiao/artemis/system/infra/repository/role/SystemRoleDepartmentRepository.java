package com.aotemiao.artemis.system.infra.repository.role;

import com.aotemiao.artemis.system.infra.dataobject.role.SystemRoleDepartmentDO;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SystemRoleDepartmentRepository extends CrudRepository<SystemRoleDepartmentDO, Long> {

    List<SystemRoleDepartmentDO> findAllByRoleIdOrderById(Long roleId);
}
