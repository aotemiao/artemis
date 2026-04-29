package com.aotemiao.artemis.system.app.command.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTenantCmdExeTest {

    @Mock
    private TenantGateway tenantGateway;

    @Mock
    private TenantPackageGateway tenantPackageGateway;

    @Mock
    private TenantBootstrapService tenantBootstrapService;

    @InjectMocks
    private CreateTenantCmdExe createTenantCmdExe;

    @Test
    void execute_whenCompanyAndPackageAvailable_createsTenant() {
        CreateTenantCmd cmd = new CreateTenantCmd(
                "阿特米斯科技", "张三", "13800000000", "91310000MA1", "上海市", "demo.artemis.com", "示例租户", 1L, null, 100, "基础说明");
        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.setId(1L);
        tenantPackage.setMenuIds(List.of(10L, 20L));
        Tenant saved = new Tenant();
        saved.setId(1L);
        saved.setTenantNo("123456");
        saved.setCompanyName("阿特米斯科技");
        when(tenantGateway.existsByCompanyName("阿特米斯科技", null)).thenReturn(false);
        when(tenantPackageGateway.findById(1L)).thenReturn(Optional.of(tenantPackage));
        when(tenantGateway.existsByTenantNo(anyString(), isNull())).thenReturn(false);
        when(tenantGateway.save(any(Tenant.class))).thenReturn(saved);

        Tenant result = createTenantCmdExe.execute(cmd);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTenantNo()).hasSize(6);
        verify(tenantBootstrapService).bootstrap(result);
        verify(tenantGateway).save(any(Tenant.class));
    }

    @Test
    void execute_whenCompanyNameDuplicated_throwsBizException() {
        when(tenantGateway.existsByCompanyName("阿特米斯科技", null)).thenReturn(true);

        assertThatThrownBy(() -> createTenantCmdExe.execute(
                        new CreateTenantCmd("阿特米斯科技", null, null, null, null, null, null, 1L, null, null, null)))
                .isInstanceOf(BizException.class);
    }

    @Test
    void execute_whenPackageMissing_throwsBizException() {
        when(tenantGateway.existsByCompanyName("阿特米斯科技", null)).thenReturn(false);
        when(tenantPackageGateway.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> createTenantCmdExe.execute(
                        new CreateTenantCmd("阿特米斯科技", null, null, null, null, null, null, 1L, null, null, null)))
                .isInstanceOf(BizException.class);
    }
}
