package com.aotemiao.artemis.resource.infra.gateway.file;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.jdbc.support.PageConversion;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import com.aotemiao.artemis.resource.infra.converter.file.OssFileConverter;
import com.aotemiao.artemis.resource.infra.repository.file.OssFileRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** OSS 文件记录 Gateway 实现。 */
@Component
public class OssFileGatewayImpl implements OssFileGateway {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects repository as a managed collaborator; this gateway does not expose it.")
    private final OssFileRepository repository;

    public OssFileGatewayImpl(OssFileRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OssFile save(OssFile ossFile) {
        return OssFileConverter.toDomain(repository.save(OssFileConverter.toDO(ossFile)));
    }

    @Override
    public Optional<OssFile> findById(Long id) {
        return repository
                .findById(id)
                .filter(entity -> Integer.valueOf(0).equals(entity.getDeleted()))
                .map(OssFileConverter::toDomain);
    }

    @Override
    public List<OssFile> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllByIdInAndDeletedOrderByIdDesc(ids, 0).stream()
                .map(OssFileConverter::toDomain)
                .toList();
    }

    @Override
    public PageResult<OssFile> findPage(PageRequest pageRequest) {
        var page = repository.findAllByDeletedOrderByIdDesc(0, PageConversion.toPageable(pageRequest));
        var pr = PageConversion.toPageResult(page);
        return PageResult.of(
                pr.total(),
                pr.content().stream().map(OssFileConverter::toDomain).toList(),
                pr.totalPages());
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
