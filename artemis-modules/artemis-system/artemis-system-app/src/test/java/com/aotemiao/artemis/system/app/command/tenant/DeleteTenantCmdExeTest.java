package com.aotemiao.artemis.system.app.command.tenant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantGateway;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteTenantCmdExeTest {

    @Mock
    private TenantGateway tenantGateway;

    @InjectMocks
    private DeleteTenantCmdExe deleteTenantCmdExe;

    @Test
    void execute_whenTenantExists_deletesTenant() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantNo("123456");
        when(tenantGateway.findById(1L)).thenReturn(Optional.of(tenant));

        deleteTenantCmdExe.execute(new DeleteTenantCmd(1L));

        verify(tenantGateway).deleteById(1L);
    }

    @Test
    void execute_whenDefaultTenant_throwsBizException() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantNo(TenantGuard.DEFAULT_TENANT_NO);
        when(tenantGateway.findById(1L)).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> deleteTenantCmdExe.execute(new DeleteTenantCmd(1L)))
                .isInstanceOf(BizException.class);
    }
}
