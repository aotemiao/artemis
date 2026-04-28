package com.aotemiao.artemis.system.infra.repository.department;

import com.aotemiao.artemis.system.infra.dataobject.department.SystemDepartmentDO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface SystemDepartmentRepository extends CrudRepository<SystemDepartmentDO, Long> {

    Optional<SystemDepartmentDO> findByParentIdAndDeptNameAndDeleted(Long parentId, String deptName, Integer deleted);

    List<SystemDepartmentDO> findAllByDeletedOrderBySortOrderAscIdAsc(Integer deleted);
}
