package com.aotemiao.artemis.system.infra.gateway;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import com.aotemiao.artemis.system.infra.converter.LookupConverter;
import com.aotemiao.artemis.system.infra.dataobject.LookupItemDO;
import com.aotemiao.artemis.system.infra.dataobject.LookupTypeDO;
import com.aotemiao.artemis.system.infra.repository.LookupTypeRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LookupTypeGatewayImpl implements LookupTypeGateway {

    private final LookupTypeRepository repository;

    public LookupTypeGatewayImpl(LookupTypeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LookupType save(LookupType lookupType) {
        LookupTypeDO d = LookupConverter.toDO(lookupType);
        LookupTypeDO saved = repository.save(d);
        return LookupConverter.toDomain(saved);
    }

    @Override
    public Optional<LookupType> findById(Long id) {
        return repository.findById(id).map(LookupConverter::toDomain);
    }

    @Override
    public PageResult<LookupType> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderById(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(), pr.content().stream().map(LookupConverter::toDomain).toList(), pr.totalPages());
    }

    /** 逻辑删除。若 id 对应记录不存在则静默不操作（存在性校验由 App 层 DeleteLookupTypeCmdExe 负责）。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        repository.findById(id).ifPresent(entity -> {
            entity.setDeleted(1);
            repository.save(entity);
        });
    }

    @Override
    public List<LookupItem> findItemsByTypeCode(String typeCode) {
        return repository.findByCodeAndDeleted(typeCode, 0).map(LookupTypeDO::getItems).orElse(List.of()).stream()
                .sorted(Comparator.comparing(
                        LookupItemDO::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(LookupConverter::toItemDomain)
                .toList();
    }
}
