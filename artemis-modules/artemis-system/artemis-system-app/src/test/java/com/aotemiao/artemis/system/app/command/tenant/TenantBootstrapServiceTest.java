package com.aotemiao.artemis.system.app.command.tenant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.app.service.config.SystemConfigCache;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.gateway.role.RoleMenuBindingGateway;
import com.aotemiao.artemis.system.domain.gateway.role.SystemRoleGateway;
import com.aotemiao.artemis.system.domain.gateway.tenant.TenantPackageGateway;
import com.aotemiao.artemis.system.domain.gateway.user.SystemUserGateway;
import com.aotemiao.artemis.system.domain.gateway.user.UserRoleBindingGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import com.aotemiao.artemis.system.domain.model.user.SystemUser;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantBootstrapServiceTest {

    @Mock
    private TenantPackageGateway tenantPackageGateway;

    @Mock
    private SystemDepartmentGateway systemDepartmentGateway;

    @Mock
    private SystemRoleGateway systemRoleGateway;

    @Mock
    private RoleMenuBindingGateway roleMenuBindingGateway;

    @Mock
    private SystemUserGateway systemUserGateway;

    @Mock
    private UserRoleBindingGateway userRoleBindingGateway;

    @Mock
    private SystemConfigCache systemConfigCache;

    @InjectMocks
    private TenantBootstrapService tenantBootstrapService;

    @Test
    void bootstrap_createsRootDepartmentRoleAndUser() {
        Tenant tenant = new Tenant();
        tenant.setId(1L);
        tenant.setTenantNo("123456");
        tenant.setCompanyName("阿特米斯科技");
        tenant.setPackageId(9L);

        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.setId(9L);
        tenantPackage.setMenuIds(List.of(1L, 2L));
        SystemDepartment savedDepartment = new SystemDepartment();
        savedDepartment.setId(10L);
        SystemRole savedRole = new SystemRole();
        savedRole.setId(20L);
        SystemUser savedUser = new SystemUser();
        savedUser.setId(30L);

        when(tenantPackageGateway.findById(9L)).thenReturn(Optional.of(tenantPackage));
        when(systemDepartmentGateway.save(any(SystemDepartment.class))).thenAnswer(invocation -> {
            SystemDepartment department = invocation.getArgument(0);
            if (department.getId() == null) {
                department.setId(savedDepartment.getId());
            }
            return department;
        });
        when(systemRoleGateway.save(any(SystemRole.class))).thenReturn(savedRole);
        when(systemUserGateway.save(any(SystemUser.class))).thenReturn(savedUser);
        when(systemConfigCache.getValue("sys.user.initPassword")).thenReturn(Optional.of("init@123"));

        tenantBootstrapService.bootstrap(tenant);

        verify(roleMenuBindingGateway).replaceMenus(20L, List.of(1L, 2L));
        verify(userRoleBindingGateway).replaceRoles(30L, List.of(20L));
        verify(systemDepartmentGateway, times(2)).save(any(SystemDepartment.class));
        verify(systemRoleGateway).save(any(SystemRole.class));
        verify(systemUserGateway).save(any(SystemUser.class));
    }
}
