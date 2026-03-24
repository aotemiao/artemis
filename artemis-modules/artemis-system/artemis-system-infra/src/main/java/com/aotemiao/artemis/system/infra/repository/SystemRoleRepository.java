package com.aotemiao.artemis.system.infra.repository;

import com.aotemiao.artemis.system.infra.dataobject.SystemRoleDO;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SystemRoleRepository extends CrudRepository<SystemRoleDO, Long> {

    Page<SystemRoleDO> findAllByDeletedOrderById(Integer deleted, Pageable pageable);

    Optional<SystemRoleDO> findByRoleKeyAndDeleted(String roleKey, Integer deleted);

    Optional<SystemRoleDO> findByRoleNameAndDeleted(String roleName, Integer deleted);

    List<SystemRoleDO> findAllByIdInAndDeletedOrderById(Collection<Long> ids, Integer deleted);
}
