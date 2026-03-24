package com.aotemiao.artemis.system.domain.gateway;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 系统角色目录 Gateway。 */
public interface SystemRoleGateway {

    SystemRole save(SystemRole systemRole);

    Optional<SystemRole> findById(Long id);

    Optional<SystemRole> findByRoleKey(String roleKey);

    Optional<SystemRole> findByRoleName(String roleName);

    List<SystemRole> findByIds(Collection<Long> ids);

    PageResult<SystemRole> findPage(PageRequest pageRequest);
}
