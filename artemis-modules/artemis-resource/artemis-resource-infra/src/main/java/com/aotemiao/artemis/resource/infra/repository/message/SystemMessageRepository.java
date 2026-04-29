package com.aotemiao.artemis.resource.infra.repository.message;

import com.aotemiao.artemis.resource.infra.dataobject.message.SystemMessageDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SystemMessageRepository extends CrudRepository<SystemMessageDO, Long> {

    Page<SystemMessageDO> findAllByRecipientUserIdAndDeletedOrderByIdDesc(
            Long recipientUserId, Integer deleted, Pageable pageable);
}
