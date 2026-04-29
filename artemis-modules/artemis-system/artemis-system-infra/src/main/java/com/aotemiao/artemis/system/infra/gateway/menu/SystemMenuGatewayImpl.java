package com.aotemiao.artemis.system.infra.gateway.menu;

import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.infra.converter.menu.SystemMenuConverter;
import com.aotemiao.artemis.system.infra.repository.menu.SystemMenuRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemMenuGatewayImpl implements SystemMenuGateway {

    private final SystemMenuRepository systemMenuRepository;

    public SystemMenuGatewayImpl(SystemMenuRepository systemMenuRepository) {
        this.systemMenuRepository = systemMenuRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemMenu save(SystemMenu systemMenu) {
        return SystemMenuConverter.toDomain(systemMenuRepository.save(SystemMenuConverter.toDO(systemMenu)));
    }

    @Override
    public Optional<SystemMenu> findById(Long id) {
        return systemMenuRepository
                .findById(id)
                .filter(menu -> Integer.valueOf(0).equals(menu.getDeleted()))
                .map(SystemMenuConverter::toDomain);
    }

    @Override
    public Optional<SystemMenu> findByParentIdAndMenuName(Long parentId, String menuName) {
        return systemMenuRepository
                .findByParentIdAndMenuNameAndDeleted(parentId, menuName, 0)
                .map(SystemMenuConverter::toDomain);
    }

    @Override
    public Optional<SystemMenu> findByPath(String path) {
        return systemMenuRepository.findByPathAndDeleted(path, 0).map(SystemMenuConverter::toDomain);
    }

    @Override
    public List<SystemMenu> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return systemMenuRepository.findAllByIdInAndDeletedOrderByParentIdAscSortOrderAscIdAsc(ids, 0).stream()
                .map(SystemMenuConverter::toDomain)
                .toList();
    }

    @Override
    public List<SystemMenu> findAll() {
        return systemMenuRepository.findAllByDeletedOrderByParentIdAscSortOrderAscIdAsc(0).stream()
                .map(SystemMenuConverter::toDomain)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        var menus = systemMenuRepository.findAllByIdInAndDeletedOrderByParentIdAscSortOrderAscIdAsc(ids, 0);
        menus.forEach(menu -> menu.setDeleted(1));
        systemMenuRepository.saveAll(menus);
    }
}
