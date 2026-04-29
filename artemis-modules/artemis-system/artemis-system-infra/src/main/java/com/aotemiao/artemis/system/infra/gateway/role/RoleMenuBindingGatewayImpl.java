package com.aotemiao.artemis.system.infra.gateway.role;

import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.infra.converter.menu.SystemMenuConverter;
import com.aotemiao.artemis.system.infra.dataobject.role.SystemRoleMenuDO;
import com.aotemiao.artemis.system.infra.repository.menu.SystemMenuRepository;
import com.aotemiao.artemis.system.infra.repository.role.SystemRoleMenuRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleMenuBindingGatewayImpl implements RoleMenuBindingGateway {

    private final SystemRoleMenuRepository systemRoleMenuRepository;
    private final SystemMenuRepository systemMenuRepository;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repositories as managed collaborators; this gateway does not expose them.")
    public RoleMenuBindingGatewayImpl(
            SystemRoleMenuRepository systemRoleMenuRepository, SystemMenuRepository systemMenuRepository) {
        this.systemRoleMenuRepository = systemRoleMenuRepository;
        this.systemMenuRepository = systemMenuRepository;
    }

    @Override
    public List<SystemMenu> findMenusByRoleId(Long roleId) {
        List<Long> menuIds = systemRoleMenuRepository.findAllByRoleIdOrderById(roleId).stream()
                .map(SystemRoleMenuDO::getMenuId)
                .distinct()
                .toList();
        return findMenusByIds(menuIds);
    }

    @Override
    public List<SystemMenu> findMenusByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = systemRoleMenuRepository.findAllByRoleIdInOrderById(roleIds).stream()
                .map(SystemRoleMenuDO::getMenuId)
                .distinct()
                .toList();
        return findMenusByIds(menuIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceMenus(Long roleId, List<Long> menuIds) {
        List<SystemRoleMenuDO> existingBindings = systemRoleMenuRepository.findAllByRoleIdOrderById(roleId);
        if (!existingBindings.isEmpty()) {
            systemRoleMenuRepository.deleteAll(existingBindings);
        }
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        systemRoleMenuRepository.saveAll(menuIds.stream()
                .distinct()
                .map(menuId -> {
                    SystemRoleMenuDO binding = new SystemRoleMenuDO();
                    binding.setRoleId(roleId);
                    binding.setMenuId(menuId);
                    return binding;
                })
                .toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByMenuIds(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        systemRoleMenuRepository.deleteAllByMenuIdIn(menuIds);
    }

    private List<SystemMenu> findMenusByIds(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        return systemMenuRepository.findAllByIdInAndDeletedOrderByParentIdAscSortOrderAscIdAsc(menuIds, 0).stream()
                .map(SystemMenuConverter::toDomain)
                .toList();
    }
}
