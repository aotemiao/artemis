package com.aotemiao.artemis.system.domain.gateway.menu;

import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 系统菜单与权限点 Gateway。 */
public interface SystemMenuGateway {

    SystemMenu save(SystemMenu systemMenu);

    Optional<SystemMenu> findById(Long id);

    Optional<SystemMenu> findByParentIdAndMenuName(Long parentId, String menuName);

    Optional<SystemMenu> findByPath(String path);

    List<SystemMenu> findByIds(Collection<Long> ids);

    List<SystemMenu> findAll();

    void deleteByIds(Collection<Long> ids);
}
