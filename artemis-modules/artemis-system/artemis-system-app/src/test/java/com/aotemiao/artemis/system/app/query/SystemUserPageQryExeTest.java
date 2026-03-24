package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemUserPageQryExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @InjectMocks
    private SystemUserPageQryExe systemUserPageQryExe;

    @Test
    void execute_delegatesToGateway() {
        PageRequest pageRequest = new PageRequest(0, 10);
        SystemUser systemUser = new SystemUser();
        systemUser.setId(1L);
        systemUser.setUsername("admin");
        PageResult<SystemUser> expected = PageResult.of(1, List.of(systemUser), 1);
        when(systemUserGateway.findPage(pageRequest)).thenReturn(expected);

        PageResult<SystemUser> result = systemUserPageQryExe.execute(new SystemUserPageQry(pageRequest));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getUsername()).isEqualTo("admin");
        verify(systemUserGateway).findPage(pageRequest);
    }
}
