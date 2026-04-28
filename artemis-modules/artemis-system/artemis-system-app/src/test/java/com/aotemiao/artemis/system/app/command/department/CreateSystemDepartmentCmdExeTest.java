package com.aotemiao.artemis.system.app.command.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSystemDepartmentCmdExeTest {

    @Mock
    private SystemDepartmentGateway systemDepartmentGateway;

    @InjectMocks
    private CreateSystemDepartmentCmdExe createSystemDepartmentCmdExe;

    @Test
    void execute_whenParentIsNormal_createsDepartment() {
        SystemDepartment parent = sampleDepartment(1L, 0L, "0", "总部", "NORMAL");
        SystemDepartment saved = sampleDepartment(2L, 1L, "0,1", "研发部", "NORMAL");
        when(systemDepartmentGateway.findById(1L)).thenReturn(Optional.of(parent));
        when(systemDepartmentGateway.findByParentIdAndDeptName(1L, "研发部")).thenReturn(Optional.empty());
        when(systemDepartmentGateway.save(any(SystemDepartment.class))).thenReturn(saved);

        SystemDepartment result = createSystemDepartmentCmdExe.execute(new CreateSystemDepartmentCmd(
                1L, "研发部", "DEPT", 10, 1L, "13800138000", "dev@example.com", "NORMAL", null));

        assertThat(result.getAncestors()).isEqualTo("0,1");
        verify(systemDepartmentGateway).save(any(SystemDepartment.class));
    }

    @Test
    void execute_whenNameDuplicated_throwsBizException() {
        when(systemDepartmentGateway.findByParentIdAndDeptName(0L, "总部"))
                .thenReturn(Optional.of(sampleDepartment(1L, 0L, "0", "总部", "NORMAL")));

        assertThatThrownBy(() -> createSystemDepartmentCmdExe.execute(
                        new CreateSystemDepartmentCmd(0L, "总部", "COMPANY", 0, null, null, null, "NORMAL", null)))
                .isInstanceOf(BizException.class);
    }

    @Test
    void execute_whenParentDisabled_throwsBizException() {
        when(systemDepartmentGateway.findById(1L))
                .thenReturn(Optional.of(sampleDepartment(1L, 0L, "0", "总部", "DISABLED")));

        assertThatThrownBy(() -> createSystemDepartmentCmdExe.execute(
                        new CreateSystemDepartmentCmd(1L, "研发部", "DEPT", 10, null, null, null, "NORMAL", null)))
                .isInstanceOf(BizException.class);
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
