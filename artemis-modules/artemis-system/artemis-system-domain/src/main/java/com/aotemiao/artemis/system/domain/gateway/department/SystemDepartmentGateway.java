package com.aotemiao.artemis.system.domain.gateway.department;

import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import java.util.List;
import java.util.Optional;

/** 系统部门 Gateway。 */
public interface SystemDepartmentGateway {

    SystemDepartment save(SystemDepartment systemDepartment);

    List<SystemDepartment> saveAll(List<SystemDepartment> systemDepartments);

    Optional<SystemDepartment> findById(Long id);

    Optional<SystemDepartment> findByParentIdAndDeptName(Long parentId, String deptName);

    List<SystemDepartment> findAll();

    void deleteById(Long id);
}
