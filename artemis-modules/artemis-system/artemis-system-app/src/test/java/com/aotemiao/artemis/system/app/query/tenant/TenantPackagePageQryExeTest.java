package com.aotemiao.artemis.system.app.query.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantPackagePageQryExeTest {

    @Mock
    private TenantPackageGateway tenantPackageGateway;

    @InjectMocks
    private TenantPackagePageQryExe tenantPackagePageQryExe;

    @Test
    void execute_returnsGatewayPage() {
        PageRequest pageRequest = new PageRequest(0, 10);
        when(tenantPackageGateway.findPage(pageRequest)).thenReturn(PageResult.of(1, List.of(samplePackage()), 1));

        PageResult<TenantPackage> result = tenantPackagePageQryExe.execute(new TenantPackagePageQry(pageRequest));

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.content()).extracting(TenantPackage::getPackageName).containsExactly("标准版");
    }

    private static TenantPackage samplePackage() {
        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.setId(1L);
        tenantPackage.setPackageName("标准版");
        return tenantPackage;
    }
}
