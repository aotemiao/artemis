package com.aotemiao.artemis.system.app.command.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSystemMenuCmdExeTest {

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @InjectMocks
    private UpdateSystemMenuCmdExe updateSystemMenuCmdExe;

    @Test
    void execute_whenMenuExists_updatesFields() {
        SystemMenu existing = new SystemMenu();
        existing.setId(1L);
        existing.setParentId(0L);
        existing.setMenuType(SystemMenu.TYPE_MENU);
        existing.setMenuName("用户管理");
        existing.setEnabled(true);
        when(systemMenuGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(systemMenuGateway.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SystemMenu result = updateSystemMenuCmdExe.execute(
                new UpdateSystemMenuCmd(1L, 0L, "BUTTON", "新增用户", 20, null, null, "system:user:add", true, false));

        assertThat(result.getMenuType()).isEqualTo(SystemMenu.TYPE_BUTTON);
        assertThat(result.getPermissionCode()).isEqualTo("system:user:add");
        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void execute_whenMenuMissing_throwsBizException() {
        when(systemMenuGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateSystemMenuCmdExe.execute(new UpdateSystemMenuCmd(
                        99L, 0L, "MENU", "用户管理", 10, "/system/users", null, "system:user:list", true, true)))
                .isInstanceOf(BizException.class);
    }
}
