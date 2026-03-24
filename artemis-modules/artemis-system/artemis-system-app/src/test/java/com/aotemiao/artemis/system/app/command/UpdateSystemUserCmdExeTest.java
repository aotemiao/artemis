package com.aotemiao.artemis.system.app.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.SystemUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSystemUserCmdExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @InjectMocks
    private UpdateSystemUserCmdExe updateSystemUserCmdExe;

    @Test
    void execute_whenUserExists_updatesUser() {
        SystemUser existing = new SystemUser();
        existing.setId(1L);
        existing.setUsername("admin");
        existing.setDisplayName("旧名称");
        existing.setPassword("old");
        existing.setEnabled(true);
        when(systemUserGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(systemUserGateway.save(any(SystemUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemUser result = updateSystemUserCmdExe.execute(new UpdateSystemUserCmd(1L, "新名称", "new", false));

        assertThat(result.getDisplayName()).isEqualTo("新名称");
        assertThat(result.getPassword()).isEqualTo("new");
        assertThat(result.isEnabled()).isFalse();
        verify(systemUserGateway).save(existing);
    }

    @Test
    void execute_whenUserMissing_throwsBizException() {
        when(systemUserGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateSystemUserCmdExe.execute(new UpdateSystemUserCmd(99L, "新名称", null, true)))
                .isInstanceOf(BizException.class);
    }
}
