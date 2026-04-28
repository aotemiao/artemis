package com.aotemiao.artemis.system.app.command.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSystemDepartmentCmdExeTest {

    @Mock
    private SystemDepartmentGateway systemDepartmentGateway;

    @InjectMocks
    private UpdateSystemDepartmentCmdExe updateSystemDepartmentCmdExe;

    @Test
    void execute_whenMovingDepartment_updatesDescendantAncestors() {
        SystemDepartment current = sampleDepartment(2L, 1L, "0,1", "研发部", "NORMAL");
        SystemDepartment newParent = sampleDepartment(3L, 0L, "0", "产品部", "NORMAL");
        SystemDepartment child = sampleDepartment(4L, 2L, "0,1,2", "平台组", "NORMAL");
        when(systemDepartmentGateway.findById(2L)).thenReturn(Optional.of(current));
        when(systemDepartmentGateway.findById(3L)).thenReturn(Optional.of(newParent));
        when(systemDepartmentGateway.findByParentIdAndDeptName(3L, "研发中心")).thenReturn(Optional.empty());
        when(systemDepartmentGateway.save(any(SystemDepartment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(systemDepartmentGateway.findAll()).thenReturn(List.of(current, newParent, child));

        SystemDepartment result = updateSystemDepartmentCmdExe.execute(
                new UpdateSystemDepartmentCmd(2L, 3L, "研发中心", "DEPT", 10, null, null, null, "NORMAL", null));

        assertThat(result.getAncestors()).isEqualTo("0,3");
        verify(systemDepartmentGateway).saveAll(any());
    }

    private static SystemDepartment sampleDepartment(
            Long id, Long parentId, String ancestors, String deptName, String status) {
        SystemDepartment department = new SystemDepartment();
        department.setId(id);
        department.setParentId(parentId);
        department.setAncestors(ancestors);
        department.setDeptName(deptName);
        department.setStatus(status);
        return department;
    }
}
