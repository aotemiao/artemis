package com.aotemiao.artemis.system.infra.gateway.post;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import com.aotemiao.artemis.system.infra.converter.post.SystemPostConverter;
import com.aotemiao.artemis.system.infra.repository.post.SystemPostRepository;
import com.aotemiao.artemis.system.infra.repository.post.SystemUserPostRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SystemPostGatewayImpl implements SystemPostGateway {

    private final SystemPostRepository repository;
    private final SystemUserPostRepository userPostRepository;

    public SystemPostGatewayImpl(SystemPostRepository repository, SystemUserPostRepository userPostRepository) {
        this.repository = repository;
        this.userPostRepository = userPostRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemPost save(SystemPost systemPost) {
        return SystemPostConverter.toDomain(repository.save(SystemPostConverter.toDO(systemPost)));
    }

    @Override
    public Optional<SystemPost> findById(Long id) {
        return repository.findById(id).filter(d -> d.getDeleted() == 0).map(SystemPostConverter::toDomain);
    }

    @Override
    public Optional<SystemPost> findByPostCode(String postCode) {
        return repository.findByPostCodeAndDeleted(postCode, 0).map(SystemPostConverter::toDomain);
    }

    @Override
    public Optional<SystemPost> findByDeptIdAndPostName(Long deptId, String postName) {
        return repository.findByDeptIdAndPostNameAndDeleted(deptId, postName, 0).map(SystemPostConverter::toDomain);
    }

    @Override
    public PageResult<SystemPost> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderBySortOrderAscIdAsc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(SystemPostConverter::toDomain).toList(),
                pr.totalPages());
    }

    @Override
    public List<SystemPost> findAll() {
        return repository.findAllByDeletedOrderBySortOrderAscIdAsc(0).stream()
                .map(SystemPostConverter::toDomain)
                .toList();
    }

    @Override
    public long countUsersByPostId(Long postId) {
        return userPostRepository.countByPostId(postId);
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
