package com.aotemiao.artemis.system.infra.gateway;

import com.aotemiao.artemis.system.domain.gateway.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import com.aotemiao.artemis.system.infra.converter.SystemRoleConverter;
import com.aotemiao.artemis.system.infra.dataobject.SystemUserRoleDO;
import com.aotemiao.artemis.system.infra.repository.SystemRoleRepository;
import com.aotemiao.artemis.system.infra.repository.SystemUserRoleRepository;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserRoleBindingGatewayImpl implements UserRoleBindingGateway {

    private final SystemUserRoleRepository systemUserRoleRepository;
    private final SystemRoleRepository systemRoleRepository;

    public UserRoleBindingGatewayImpl(
            SystemUserRoleRepository systemUserRoleRepository, SystemRoleRepository systemRoleRepository) {
        this.systemUserRoleRepository = systemUserRoleRepository;
        this.systemRoleRepository = systemRoleRepository;
    }

    @Override
    public List<SystemRole> findRolesByUserId(Long userId) {
        List<Long> roleIds = systemUserRoleRepository.findAllByUserIdOrderById(userId).stream()
                .map(SystemUserRoleDO::getRoleId)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return systemRoleRepository.findAllByIdInAndDeletedOrderById(roleIds, 0).stream()
                .map(SystemRoleConverter::toDomain)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceRoles(Long userId, List<Long> roleIds) {
        List<SystemUserRoleDO> existingBindings = systemUserRoleRepository.findAllByUserIdOrderById(userId);
        if (!existingBindings.isEmpty()) {
            systemUserRoleRepository.deleteAll(existingBindings);
        }
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        systemUserRoleRepository.saveAll(roleIds.stream()
                .distinct()
                .map(roleId -> {
                    SystemUserRoleDO binding = new SystemUserRoleDO();
                    binding.setUserId(userId);
                    binding.setRoleId(roleId);
                    return binding;
                })
                .toList());
    }
}
