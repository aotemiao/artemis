package com.aotemiao.artemis.system.app.query.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListSystemDepartmentQryExeTest {

    @Mock
    private SystemDepartmentGateway systemDepartmentGateway;

    @InjectMocks
    private ListSystemDepartmentQryExe listSystemDepartmentQryExe;

    @Test
    void execute_whenExcludeIdPresent_excludesNodeAndChildren() {
        SystemDepartment root = sampleDepartment(1L, 0L, "0", "总部");
        SystemDepartment child = sampleDepartment(2L, 1L, "0,1", "研发部");
        SystemDepartment grandChild = sampleDepartment(3L, 2L, "0,1,2", "平台组");
        when(systemDepartmentGateway.findAll()).thenReturn(List.of(root, child, grandChild));

        List<SystemDepartment> result = listSystemDepartmentQryExe.execute(new ListSystemDepartmentQry(2L));

        assertThat(result).extracting(SystemDepartment::getId).containsExactly(1L);
    }

    private static SystemDepartment sampleDepartment(Long id, Long parentId, String ancestors, String deptName) {
        SystemDepartment department = new SystemDepartment();
        department.setId(id);
        department.setParentId(parentId);
        department.setAncestors(ancestors);
        department.setDeptName(deptName);
        return department;
    }
}
