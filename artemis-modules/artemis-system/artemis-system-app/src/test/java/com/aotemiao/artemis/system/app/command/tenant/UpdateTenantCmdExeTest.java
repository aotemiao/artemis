package com.aotemiao.artemis.system.app.command.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTenantCmdExeTest {

    @Mock
    private TenantGateway tenantGateway;

    @Mock
    private TenantPackageGateway tenantPackageGateway;

    @Mock
    private TenantBootstrapService tenantBootstrapService;

    @InjectMocks
    private UpdateTenantCmdExe updateTenantCmdExe;

    @Test
    void execute_whenTenantExists_updatesTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantNo("123456");
        tenant.setPackageId(2L);
        tenant.setCompanyName("阿特米斯科技");
        when(tenantGateway.findById(1L)).thenReturn(Optional.of(tenant));
        when(tenantGateway.existsByCompanyName("阿特米斯科技", 1L)).thenReturn(false);
        when(tenantGateway.save(any(Tenant.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tenant result = updateTenantCmdExe.execute(new UpdateTenantCmd(
                1L,
                "阿特米斯科技",
                "李四",
                "13900000000",
                "91310000MA2",
                "上海市浦东新区",
                "tenant.demo.artemis.com",
                "更新说明",
                LocalDateTime.now().plusDays(30),
                200,
                "备注"));

        assertThat(result.getContactName()).isEqualTo("李四");
        assertThat(result.getPackageId()).isEqualTo(2L);
        verify(tenantGateway).save(any(Tenant.class));
    }

    @Test
    void execute_whenDefaultTenant_throwsBizException() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantNo(TenantGuard.DEFAULT_TENANT_NO);
        when(tenantGateway.findById(1L)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> updateTenantCmdExe.execute(
                        new UpdateTenantCmd(1L, "默认管理租户", null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(BizException.class);
    }
}
