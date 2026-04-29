package com.aotemiao.artemis.system.app.command.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleDepartmentBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReplaceRoleDataScopeCmdExeTest {

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @Mock
    private SystemDepartmentGateway systemDepartmentGateway;

    @Mock
    private RoleDepartmentBindingGateway roleDepartmentBindingGateway;

    @InjectMocks
    private ReplaceRoleDataScopeCmdExe replaceRoleDataScopeCmdExe;

    @Test
    void execute_whenRoleAndDepartmentsExist_replacesDataScope() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        role.setRoleKey("auditor");
        role.setRoleName("审计员");
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(role));
        when(systemDepartmentGateway.findAll()).thenReturn(List.of(department(10L), department(20L)));
        when(systemRoleGateway.save(role)).thenReturn(role);
        when(roleDepartmentBindingGateway.findDepartmentIdsByRoleId(1L)).thenReturn(List.of(10L, 20L));

        List<Long> result =
                replaceRoleDataScopeCmdExe.execute(new ReplaceRoleDataScopeCmd(1L, "CUSTOM", List.of(10L, 20L, 20L)));

        assertThat(role.getDataScope()).isEqualTo("CUSTOM");
        assertThat(result).containsExactly(10L, 20L);
        verify(roleDepartmentBindingGateway).replaceDepartments(1L, List.of(10L, 20L));
    }

    @Test
    void execute_whenDepartmentMissing_throwsBizException() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(role));
        when(systemDepartmentGateway.findAll()).thenReturn(List.of(department(10L)));

        assertThatThrownBy(() ->
                        replaceRoleDataScopeCmdExe.execute(new ReplaceRoleDataScopeCmd(1L, "CUSTOM", List.of(99L))))
                .isInstanceOf(BizException.class);
    }

    private static SystemDepartment department(Long id) {
        SystemDepartment department = new SystemDepartment();
        department.setId(id);
        department.setDeptName("部门" + id);
        department.setStatus("NORMAL");
        return department;
    }
}
