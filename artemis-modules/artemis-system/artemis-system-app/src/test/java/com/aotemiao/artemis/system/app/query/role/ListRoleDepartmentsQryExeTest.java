package com.aotemiao.artemis.system.app.query.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.role.RoleDepartmentBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListRoleDepartmentsQryExeTest {

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @Mock
    private RoleDepartmentBindingGateway roleDepartmentBindingGateway;

    @InjectMocks
    private ListRoleDepartmentsQryExe listRoleDepartmentsQryExe;

    @Test
    void execute_whenRoleExists_returnsDepartmentIds() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.of(role));
        when(roleDepartmentBindingGateway.findDepartmentIdsByRoleId(1L)).thenReturn(List.of(10L, 20L));

        List<Long> result = listRoleDepartmentsQryExe.execute(new ListRoleDepartmentsQry(1L));

        assertThat(result).containsExactly(10L, 20L);
    }

    @Test
    void execute_whenRoleMissing_throwsBizException() {
        when(systemRoleGateway.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> listRoleDepartmentsQryExe.execute(new ListRoleDepartmentsQry(1L)))
                .isInstanceOf(BizException.class);
    }
}
