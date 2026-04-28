package com.aotemiao.artemis.system.infra.repository.post;

import com.aotemiao.artemis.system.infra.dataobject.post.SystemPostDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface SystemPostRepository extends CrudRepository<SystemPostDO, Long> {

    Optional<SystemPostDO> findByPostCodeAndDeleted(String postCode, Integer deleted);

    Optional<SystemPostDO> findByDeptIdAndPostNameAndDeleted(Long deptId, String postName, Integer deleted);

    Page<SystemPostDO> findAllByDeletedOrderBySortOrderAscIdAsc(Integer deleted, Pageable pageable);

    List<SystemPostDO> findAllByDeletedOrderBySortOrderAscIdAsc(Integer deleted);
}
