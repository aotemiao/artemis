package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemRolePageQryExeTest {

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @InjectMocks
    private SystemRolePageQryExe systemRolePageQryExe;

    @Test
    void execute_returnsPagedRoles() {
        SystemRole role = new SystemRole();
        role.setId(1L);
        role.setRoleKey("super-admin");
        PageRequest pageRequest = new PageRequest(0, 10);
        when(systemRoleGateway.findPage(pageRequest)).thenReturn(PageResult.of(1, List.of(role), 1));

        PageResult<SystemRole> pageResult = systemRolePageQryExe.execute(new SystemRolePageQry(pageRequest));

        assertThat(pageResult.total()).isEqualTo(1);
        assertThat(pageResult.content()).containsExactly(role);
    }
}
