package com.aotemiao.artemis.resource.infra.repository.config;

import com.aotemiao.artemis.resource.infra.dataobject.config.OssConfigDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface OssConfigRepository extends CrudRepository<OssConfigDO, Long> {

    Page<OssConfigDO> findAllByDeletedOrderByIdDesc(Integer deleted, Pageable pageable);

    Optional<OssConfigDO> findByConfigKeyAndDeleted(String configKey, Integer deleted);

    List<OssConfigDO> findAllByDefaultFlagAndDeleted(Integer defaultFlag, Integer deleted);
}
