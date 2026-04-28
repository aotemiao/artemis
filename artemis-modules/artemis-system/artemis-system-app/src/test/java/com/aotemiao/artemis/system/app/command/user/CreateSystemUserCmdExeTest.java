package com.aotemiao.artemis.system.app.command.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.model.user.SystemUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSystemUserCmdExeTest {

    @Mock
    private SystemUserGateway systemUserGateway;

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private CreateSystemUserCmdExe createSystemUserCmdExe;

    @Test
    void execute_whenUsernameAvailable_createsUser() {
        CreateSystemUserCmd cmd = new CreateSystemUserCmd("admin", "管理员", "123456");
        SystemUser saved = new SystemUser();
        saved.setId(1L);
        saved.setUsername("admin");
        saved.setDisplayName("管理员");
        saved.setPassword("123456");
        saved.setEnabled(true);
        when(systemUserGateway.findByUsername("admin")).thenReturn(Optional.empty());
        when(systemUserGateway.save(any(SystemUser.class))).thenReturn(saved);

        SystemUser result = createSystemUserCmdExe.execute(cmd);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("admin");
        verify(systemUserGateway).findByUsername("admin");
        verify(systemUserGateway).save(any(SystemUser.class));
    }

    @Test
    void execute_whenUsernameExists_throwsBizException() {
        CreateSystemUserCmd cmd = new CreateSystemUserCmd("admin", "管理员", "123456");
        SystemUser existing = new SystemUser();
        existing.setId(1L);
        existing.setUsername("admin");
        when(systemUserGateway.findByUsername("admin")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> createSystemUserCmdExe.execute(cmd)).isInstanceOf(BizException.class);
    }

    @Test
    void execute_whenPasswordBlank_usesInitialPasswordConfig() {
        CreateSystemUserCmd cmd = new CreateSystemUserCmd("demo", "演示用户", "");
        when(systemUserGateway.findByUsername("demo")).thenReturn(Optional.empty());
        when(systemConfigCache.getValue("sys.user.initPassword")).thenReturn(Optional.of("init@123"));
        when(systemUserGateway.save(any(SystemUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemUser result = createSystemUserCmdExe.execute(cmd);

        assertThat(result.getPassword()).isEqualTo("init@123");
    }
}
