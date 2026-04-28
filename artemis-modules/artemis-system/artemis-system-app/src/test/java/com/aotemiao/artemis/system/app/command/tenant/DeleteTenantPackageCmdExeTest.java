package com.aotemiao.artemis.system.app.command.tenant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteTenantPackageCmdExeTest {

    @Mock
    private TenantPackageGateway tenantPackageGateway;

    @InjectMocks
    private DeleteTenantPackageCmdExe deleteTenantPackageCmdExe;

    @Test
    void execute_whenNotUsedByTenant_deletesPackage() {
        when(tenantPackageGateway.findById(1L)).thenReturn(Optional.of(samplePackage()));
        when(tenantPackageGateway.isUsedByTenant(1L)).thenReturn(false);

        deleteTenantPackageCmdExe.execute(new DeleteTenantPackageCmd(1L));

        verify(tenantPackageGateway).deleteById(1L);
    }

    @Test
    void execute_whenUsedByTenant_throwsBizException() {
        when(tenantPackageGateway.findById(1L)).thenReturn(Optional.of(samplePackage()));
        when(tenantPackageGateway.isUsedByTenant(1L)).thenReturn(true);

        assertThatThrownBy(() -> deleteTenantPackageCmdExe.execute(new DeleteTenantPackageCmd(1L)))
                .isInstanceOf(BizException.class);
    }

    private static TenantPackage samplePackage() {
        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.setId(1L);
        tenantPackage.setPackageName("标准版");
        return tenantPackage;
    }
}
