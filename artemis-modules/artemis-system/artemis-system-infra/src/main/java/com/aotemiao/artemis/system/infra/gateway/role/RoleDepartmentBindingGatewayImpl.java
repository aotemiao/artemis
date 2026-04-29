package com.aotemiao.artemis.system.infra.gateway.role;

import com.aotemiao.artemis.system.domain.gateway.role.RoleDepartmentBindingGateway;
import com.aotemiao.artemis.system.infra.dataobject.role.SystemRoleDepartmentDO;
import com.aotemiao.artemis.system.infra.repository.role.SystemRoleDepartmentRepository;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleDepartmentBindingGatewayImpl implements RoleDepartmentBindingGateway {

    private final SystemRoleDepartmentRepository systemRoleDepartmentRepository;

    public RoleDepartmentBindingGatewayImpl(SystemRoleDepartmentRepository systemRoleDepartmentRepository) {
        this.systemRoleDepartmentRepository = systemRoleDepartmentRepository;
    }

    @Override
    public List<Long> findDepartmentIdsByRoleId(Long roleId) {
        return systemRoleDepartmentRepository.findAllByRoleIdOrderById(roleId).stream()
                .map(SystemRoleDepartmentDO::getDepartmentId)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceDepartments(Long roleId, List<Long> departmentIds) {
        List<SystemRoleDepartmentDO> existingBindings = systemRoleDepartmentRepository.findAllByRoleIdOrderById(roleId);
        if (!existingBindings.isEmpty()) {
            systemRoleDepartmentRepository.deleteAll(existingBindings);
        }
        if (departmentIds == null || departmentIds.isEmpty()) {
            return;
        }
        systemRoleDepartmentRepository.saveAll(departmentIds.stream()
                .distinct()
                .map(departmentId -> {
                    SystemRoleDepartmentDO binding = new SystemRoleDepartmentDO();
                    binding.setRoleId(roleId);
                    binding.setDepartmentId(departmentId);
                    return binding;
                })
                .toList());
    }
}
