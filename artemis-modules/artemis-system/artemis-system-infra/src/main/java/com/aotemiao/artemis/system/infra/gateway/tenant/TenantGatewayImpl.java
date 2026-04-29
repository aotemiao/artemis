package com.aotemiao.artemis.system.infra.gateway.tenant;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import com.aotemiao.artemis.system.infra.converter.tenant.TenantConverter;
import com.aotemiao.artemis.system.infra.repository.tenant.TenantRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TenantGatewayImpl implements TenantGateway {

    private final TenantRepository repository;

    public TenantGatewayImpl(TenantRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Tenant save(Tenant tenant) {
        return TenantConverter.toDomain(repository.save(TenantConverter.toDO(tenant)));
    }

    @Override
    public Optional<Tenant> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(TenantConverter::toDomain);
    }

    @Override
    public Optional<Tenant> findByTenantNo(String tenantNo) {
        return repository.findByTenantNoAndDeleted(tenantNo, 0).map(TenantConverter::toDomain);
    }

    @Override
    public Optional<Tenant> findByCompanyName(String companyName) {
        return repository.findByCompanyNameAndDeleted(companyName, 0).map(TenantConverter::toDomain);
    }

    @Override
    public PageResult<Tenant> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByIdDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(), pr.content().stream().map(TenantConverter::toDomain).toList(), pr.totalPages());
    }

    @Override
    public List<Tenant> findEnabled() {
        return repository.findAllByDeletedAndStatusOrderByIdDesc(0, "NORMAL").stream()
                .map(TenantConverter::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTenantNo(String tenantNo, Long excludeId) {
        return repository
                .findByTenantNoAndDeleted(tenantNo, 0)
                .filter(entity -> excludeId == null || !excludeId.equals(entity.getId()))
                .isPresent();
    }

    @Override
    public boolean existsByCompanyName(String companyName, Long excludeId) {
        return repository
                .findByCompanyNameAndDeleted(companyName, 0)
                .filter(entity -> excludeId == null || !excludeId.equals(entity.getId()))
                .isPresent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(1);
            repository.save(entity);
        });
    }
}
