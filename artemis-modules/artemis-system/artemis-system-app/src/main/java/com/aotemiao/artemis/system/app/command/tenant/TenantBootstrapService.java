package com.aotemiao.artemis.system.app.command.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 租户初始化骨架服务。 */
@Component
public class TenantBootstrapService {

    private static final String INIT_PASSWORD_KEY = "sys.user.initPassword";
    private static final String DEFAULT_INIT_PASSWORD = "123456";

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this service does not expose them.")
    private final TenantPackageGateway tenantPackageGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this service does not expose them.")
    private final SystemDepartmentGateway systemDepartmentGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this service does not expose them.")
    private final SystemRoleGateway systemRoleGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this service does not expose them.")
    private final RoleMenuBindingGateway roleMenuBindingGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this service does not expose them.")
    private final SystemUserGateway systemUserGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateways as managed collaborators; this service does not expose them.")
    private final UserRoleBindingGateway userRoleBindingGateway;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the cache as a managed collaborator; this service does not expose it.")
    private final SystemConfigCache systemConfigCache;

    public TenantBootstrapService(
            TenantPackageGateway tenantPackageGateway,
            SystemDepartmentGateway systemDepartmentGateway,
            SystemRoleGateway systemRoleGateway,
            RoleMenuBindingGateway roleMenuBindingGateway,
            SystemUserGateway systemUserGateway,
            UserRoleBindingGateway userRoleBindingGateway,
            SystemConfigCache systemConfigCache) {
        this.tenantPackageGateway = tenantPackageGateway;
        this.systemDepartmentGateway = systemDepartmentGateway;
        this.systemRoleGateway = systemRoleGateway;
        this.roleMenuBindingGateway = roleMenuBindingGateway;
        this.systemUserGateway = systemUserGateway;
        this.userRoleBindingGateway = userRoleBindingGateway;
        this.systemConfigCache = systemConfigCache;
    }

    @Transactional(rollbackFor = Exception.class)
    public void bootstrap(Tenant tenant) {
        TenantPackage tenantPackage = tenantPackageGateway
                .findById(tenant.getPackageId())
                .orElseThrow(() -> new BizException(
                        CommonErrorCode.NOT_FOUND, "Tenant package not found: " + tenant.getPackageId()));

        SystemDepartment rootDepartment = new SystemDepartment();
        rootDepartment.setParentId(0L);
        rootDepartment.setAncestors("0");
        rootDepartment.setDeptName(tenant.getCompanyName());
        rootDepartment.setDeptCategory("TENANT");
        rootDepartment.setSortOrder(0);
        rootDepartment.setStatus("NORMAL");
        rootDepartment.setRemarks("租户初始化根部门");
        rootDepartment = systemDepartmentGateway.save(rootDepartment);

        SystemRole tenantAdminRole = new SystemRole();
        tenantAdminRole.setRoleKey("tenant-admin-" + tenant.getTenantNo());
        tenantAdminRole.setRoleName(tenant.getCompanyName() + "管理员");
        tenantAdminRole.setEnabled(true);
        tenantAdminRole = systemRoleGateway.save(tenantAdminRole);

        roleMenuBindingGateway.replaceMenus(tenantAdminRole.getId(), tenantPackage.getMenuIds());

        SystemUser tenantAdmin = new SystemUser();
        tenantAdmin.setUsername(tenant.getTenantNo() + "_admin");
        tenantAdmin.setDisplayName(tenant.getCompanyName() + "管理员");
        tenantAdmin.setPassword(systemConfigCache.getValue(INIT_PASSWORD_KEY).orElse(DEFAULT_INIT_PASSWORD));
        tenantAdmin.setEnabled(true);
        tenantAdmin = systemUserGateway.save(tenantAdmin);

        userRoleBindingGateway.replaceRoles(tenantAdmin.getId(), java.util.List.of(tenantAdminRole.getId()));

        rootDepartment.setLeaderUserId(tenantAdmin.getId());
        systemDepartmentGateway.save(rootDepartment);
    }
}
