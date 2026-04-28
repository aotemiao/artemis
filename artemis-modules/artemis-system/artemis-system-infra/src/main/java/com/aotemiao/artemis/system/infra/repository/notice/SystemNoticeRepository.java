package com.aotemiao.artemis.system.infra.repository.notice;

import com.aotemiao.artemis.system.infra.dataobject.notice.SystemNoticeDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SystemNoticeRepository extends CrudRepository<SystemNoticeDO, Long> {

    Page<SystemNoticeDO> findAllByDeletedOrderById(Integer deleted, Pageable pageable);
}
