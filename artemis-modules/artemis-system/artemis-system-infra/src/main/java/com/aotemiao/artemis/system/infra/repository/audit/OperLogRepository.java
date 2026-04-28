package com.aotemiao.artemis.system.infra.repository.audit;

import com.aotemiao.artemis.system.infra.dataobject.audit.OperLogDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface OperLogRepository extends CrudRepository<OperLogDO, Long> {

    Page<OperLogDO> findAllByDeletedOrderByOperTimeDesc(Integer deleted, Pageable pageable);

    Iterable<OperLogDO> findAllByDeleted(Integer deleted);
}
