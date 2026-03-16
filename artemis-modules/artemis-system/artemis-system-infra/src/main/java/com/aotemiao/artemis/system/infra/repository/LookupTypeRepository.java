package com.aotemiao.artemis.system.infra.repository;

import com.aotemiao.artemis.system.infra.dataobject.LookupTypeDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface LookupTypeRepository extends CrudRepository<LookupTypeDO, Long> {

    /** 未删除分页查询，Spring Data JDBC 不支持 @Query 分页，使用派生方法。 */
    Page<LookupTypeDO> findAllByDeletedOrderById(Integer deleted, Pageable pageable);

    java.util.Optional<LookupTypeDO> findByCodeAndDeleted(String code, Integer deleted);
}
