package com.aotemiao.artemis.system.infra.gateway.user;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.user.SystemUser;
import com.aotemiao.artemis.system.infra.converter.user.SystemUserConverter;
import com.aotemiao.artemis.system.infra.repository.user.SystemUserRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemUserGatewayImpl implements SystemUserGateway {

    private final SystemUserRepository systemUserRepository;

    public SystemUserGatewayImpl(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemUser save(SystemUser systemUser) {
        return SystemUserConverter.toDomain(systemUserRepository.save(SystemUserConverter.toDO(systemUser)));
    }

    @Override
    public Optional<SystemUser> findById(Long id) {
        return systemUserRepository.findById(id).map(SystemUserConverter::toDomain);
    }

    @Override
    public Optional<SystemUser> findByUsername(String username) {
        return systemUserRepository.findByUsernameAndDeleted(username, 0).map(SystemUserConverter::toDomain);
    }

    @Override
    public PageResult<SystemUser> findPage(PageRequest pageRequest) {
        var page = systemUserRepository.findAllByDeletedOrderById(0, PageConversion.toPageable(pageRequest));
        var pageResult = PageConversion.toPageResult(page);
        return PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(SystemUserConverter::toDomain).toList(),
                pageResult.totalPages());
    }
}
