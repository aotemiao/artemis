package com.aotemiao.artemis.system.app.command.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSystemMenuCmdExeTest {

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @InjectMocks
    private CreateSystemMenuCmdExe createSystemMenuCmdExe;

    @Test
    void execute_whenValidCommand_savesMenu() {
        SystemMenu saved = new SystemMenu();
        saved.setId(1L);
        saved.setParentId(0L);
        saved.setMenuType(SystemMenu.TYPE_MENU);
        saved.setMenuName("用户管理");
        saved.setPath("/system/users");
        saved.setPermissionCode("system:user:list");
        saved.setEnabled(true);
        when(systemMenuGateway.save(any())).thenReturn(saved);

        SystemMenu result = createSystemMenuCmdExe.execute(new CreateSystemMenuCmd(
                0L, "menu", " 用户管理 ", 10, " /system/users ", "system/user/index", "system:user:list", true));

        assertThat(result).isSameAs(saved);
        verify(systemMenuGateway).save(any(SystemMenu.class));
    }

    @Test
    void execute_whenSameParentNameExists_throwsBizException() {
        SystemMenu existing = new SystemMenu();
        existing.setId(9L);
        when(systemMenuGateway.findByParentIdAndMenuName(0L, "用户管理")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> createSystemMenuCmdExe.execute(new CreateSystemMenuCmd(
                        0L, "MENU", "用户管理", 10, "/system/users", null, "system:user:list", true)))
                .isInstanceOf(BizException.class);
    }
}
