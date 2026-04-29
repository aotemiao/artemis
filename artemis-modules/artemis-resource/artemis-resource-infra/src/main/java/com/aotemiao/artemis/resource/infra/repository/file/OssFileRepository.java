package com.aotemiao.artemis.resource.infra.repository.file;

import com.aotemiao.artemis.resource.infra.dataobject.file.OssFileDO;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface OssFileRepository extends CrudRepository<OssFileDO, Long> {

    Page<OssFileDO> findAllByDeletedOrderByIdDesc(Integer deleted, Pageable pageable);

    List<OssFileDO> findAllByIdInAndDeletedOrderByIdDesc(Collection<Long> ids, Integer deleted);
}
