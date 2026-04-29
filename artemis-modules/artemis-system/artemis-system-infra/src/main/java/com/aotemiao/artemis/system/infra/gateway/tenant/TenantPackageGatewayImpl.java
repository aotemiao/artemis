package com.aotemiao.artemis.system.infra.gateway.tenant;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import com.aotemiao.artemis.system.infra.converter.tenant.TenantPackageConverter;
import com.aotemiao.artemis.system.infra.dataobject.tenant.TenantPackageDO;
import com.aotemiao.artemis.system.infra.dataobject.tenant.TenantPackageMenuDO;
import com.aotemiao.artemis.system.infra.repository.tenant.TenantPackageMenuRepository;
import com.aotemiao.artemis.system.infra.repository.tenant.TenantPackageRepository;
import com.aotemiao.artemis.system.infra.repository.tenant.TenantRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TenantPackageGatewayImpl implements TenantPackageGateway {

    private final TenantPackageRepository repository;

    private final TenantPackageMenuRepository menuRepository;

    private final TenantRepository tenantRepository;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repositories as managed collaborators; this gateway does not expose them.")
    public TenantPackageGatewayImpl(
            TenantPackageRepository repository,
            TenantPackageMenuRepository menuRepository,
            TenantRepository tenantRepository) {
        this.repository = repository;
        this.menuRepository = menuRepository;
        this.tenantRepository = tenantRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TenantPackage save(TenantPackage tenantPackage) {
        TenantPackageDO saved = repository.save(TenantPackageConverter.toDO(tenantPackage));
        menuRepository.deleteAllByPackageId(saved.getId());
        menuRepository.saveAll(tenantPackage.getMenuIds().stream()
                .map(menuId -> toMenuDO(saved.getId(), menuId))
                .toList());
        return TenantPackageConverter.toDomain(saved, tenantPackage.getMenuIds());
    }

    @Override
    public Optional<TenantPackage> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(this::toDomainWithMenus);
    }

    @Override
    public PageResult<TenantPackage> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByIdDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(), pr.content().stream().map(this::toDomainWithMenus).toList(), pr.totalPages());
    }

    @Override
    public List<TenantPackage> findEnabled() {
        return repository.findAllByDeletedAndEnabledOrderByIdDesc(0, true).stream()
                .map(this::toDomainWithMenus)
                .toList();
    }

    @Override
    public boolean existsByPackageName(String packageName, Long excludeId) {
        return repository
                .findByPackageNameAndDeleted(packageName, 0)
                .filter(entity -> excludeId == null || !excludeId.equals(entity.getId()))
                .isPresent();
    }

    @Override
    public boolean isUsedByTenant(Long packageId) {
        return tenantRepository.existsByPackageIdAndDeleted(packageId, 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(1);
            repository.save(entity);
        });
        menuRepository.deleteAllByPackageId(id);
    }

    private TenantPackage toDomainWithMenus(TenantPackageDO source) {
        return TenantPackageConverter.toDomain(source, findMenuIds(source.getId()));
    }

    private List<Long> findMenuIds(Long packageId) {
        return menuRepository.findAllByPackageId(packageId).stream()
                .map(TenantPackageMenuDO::getMenuId)
                .toList();
    }

    private TenantPackageMenuDO toMenuDO(Long packageId, Long menuId) {
        TenantPackageMenuDO target = new TenantPackageMenuDO();
        target.setPackageId(packageId);
        target.setMenuId(menuId);
        return target;
    }
}
