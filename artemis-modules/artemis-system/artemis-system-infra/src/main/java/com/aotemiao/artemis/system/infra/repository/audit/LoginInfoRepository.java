package com.aotemiao.artemis.system.infra.repository.audit;

import com.aotemiao.artemis.system.infra.dataobject.audit.LoginInfoDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface LoginInfoRepository extends CrudRepository<LoginInfoDO, Long> {

    Page<LoginInfoDO> findAllByDeletedOrderByLoginTimeDesc(Integer deleted, Pageable pageable);

    Iterable<LoginInfoDO> findAllByDeleted(Integer deleted);
}
