package com.aotemiao.artemis.system.infra.repository.role;

import com.aotemiao.artemis.system.infra.dataobject.role.SystemRoleMenuDO;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SystemRoleMenuRepository extends CrudRepository<SystemRoleMenuDO, Long> {

    List<SystemRoleMenuDO> findAllByRoleIdOrderById(Long roleId);

    List<SystemRoleMenuDO> findAllByRoleIdInOrderById(Collection<Long> roleIds);
}
