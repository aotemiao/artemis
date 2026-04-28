package com.aotemiao.artemis.system.domain.gateway.user;

import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import java.util.List;

/** 用户与角色绑定 Gateway。 */
public interface UserRoleBindingGateway {

    List<SystemRole> findRolesByUserId(Long userId);

    void replaceRoles(Long userId, List<Long> roleIds);
}
