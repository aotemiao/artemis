package com.aotemiao.artemis.system.app.command.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.menu.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTenantPackageCmdExeTest {

    @Mock
    private TenantPackageGateway tenantPackageGateway;

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @InjectMocks
    private CreateTenantPackageCmdExe createTenantPackageCmdExe;

    @Test
    void execute_whenMenuIdsValid_createsPackage() {
        when(tenantPackageGateway.existsByPackageName("标准版", null)).thenReturn(false);
        when(systemMenuGateway.findByIds(List.of(1L, 2L))).thenReturn(List.of(sampleMenu(1L), sampleMenu(2L)));
        when(tenantPackageGateway.save(any(TenantPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TenantPackage result =
                createTenantPackageCmdExe.execute(new TenantPackageCmd("标准版", true, true, "默认套餐", List.of(1L, 2L, 2L)));

        assertThat(result.getPackageName()).isEqualTo("标准版");
        assertThat(result.getMenuIds()).containsExactly(1L, 2L);
        verify(tenantPackageGateway).save(any(TenantPackage.class));
    }

    @Test
    void execute_whenPackageNameDuplicated_throwsBizException() {
        when(tenantPackageGateway.existsByPackageName("标准版", null)).thenReturn(true);

        assertThatThrownBy(() ->
                        createTenantPackageCmdExe.execute(new TenantPackageCmd("标准版", true, true, null, List.of())))
                .isInstanceOf(BizException.class);
    }

    @Test
    void execute_whenMenuMissing_throwsBizException() {
        when(tenantPackageGateway.existsByPackageName("标准版", null)).thenReturn(false);
        when(systemMenuGateway.findByIds(List.of(1L, 2L))).thenReturn(List.of(sampleMenu(1L)));

        assertThatThrownBy(() -> createTenantPackageCmdExe.execute(
                        new TenantPackageCmd("标准版", true, true, null, List.of(1L, 2L))))
                .isInstanceOf(BizException.class);
    }

    private static SystemMenu sampleMenu(Long id) {
        SystemMenu menu = new SystemMenu();
        menu.setId(id);
        menu.setMenuName("菜单" + id);
        return menu;
    }
}
