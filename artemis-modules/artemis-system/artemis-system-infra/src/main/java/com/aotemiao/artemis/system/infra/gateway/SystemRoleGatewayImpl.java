package com.aotemiao.artemis.system.infra.gateway;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import com.aotemiao.artemis.system.infra.converter.SystemRoleConverter;
import com.aotemiao.artemis.system.infra.repository.SystemRoleRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemRoleGatewayImpl implements SystemRoleGateway {

    private final SystemRoleRepository systemRoleRepository;

    public SystemRoleGatewayImpl(SystemRoleRepository systemRoleRepository) {
        this.systemRoleRepository = systemRoleRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemRole save(SystemRole systemRole) {
        return SystemRoleConverter.toDomain(systemRoleRepository.save(SystemRoleConverter.toDO(systemRole)));
    }

    @Override
    public Optional<SystemRole> findById(Long id) {
        return systemRoleRepository.findById(id).map(SystemRoleConverter::toDomain);
    }

    @Override
    public Optional<SystemRole> findByRoleKey(String roleKey) {
        return systemRoleRepository.findByRoleKeyAndDeleted(roleKey, 0).map(SystemRoleConverter::toDomain);
    }

    @Override
    public Optional<SystemRole> findByRoleName(String roleName) {
        return systemRoleRepository.findByRoleNameAndDeleted(roleName, 0).map(SystemRoleConverter::toDomain);
    }

    @Override
    public List<SystemRole> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return systemRoleRepository.findAllByIdInAndDeletedOrderById(ids, 0).stream()
                .map(SystemRoleConverter::toDomain)
                .toList();
    }

    @Override
    public PageResult<SystemRole> findPage(PageRequest pageRequest) {
        var page = systemRoleRepository.findAllByDeletedOrderById(0, PageConversion.toPageable(pageRequest));
        var pageResult = PageConversion.toPageResult(page);
        return PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(SystemRoleConverter::toDomain).toList(),
                pageResult.totalPages());
    }
}
