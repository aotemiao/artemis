package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.CreateSystemRoleRequest;
import com.aotemiao.artemis.system.adapter.web.dto.ReplaceRoleDataScopeRequest;
import com.aotemiao.artemis.system.adapter.web.dto.ReplaceRoleMenusRequest;
import com.aotemiao.artemis.system.adapter.web.dto.SystemMenuDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemRoleDTO;
import com.aotemiao.artemis.system.adapter.web.dto.UpdateSystemRoleRequest;
import com.aotemiao.artemis.system.app.command.role.CreateSystemRoleCmd;
import com.aotemiao.artemis.system.app.command.role.CreateSystemRoleCmdExe;
import com.aotemiao.artemis.system.app.command.role.ReplaceRoleDataScopeCmd;
import com.aotemiao.artemis.system.app.command.role.ReplaceRoleDataScopeCmdExe;
import com.aotemiao.artemis.system.app.command.role.ReplaceRoleMenusCmd;
import com.aotemiao.artemis.system.app.command.role.ReplaceRoleMenusCmdExe;
import com.aotemiao.artemis.system.app.command.role.UpdateSystemRoleCmd;
import com.aotemiao.artemis.system.app.command.role.UpdateSystemRoleCmdExe;
import com.aotemiao.artemis.system.app.query.menu.ListRoleMenusQry;
import com.aotemiao.artemis.system.app.query.menu.ListRoleMenusQryExe;
import com.aotemiao.artemis.system.app.query.role.FindSystemRoleByIdQry;
import com.aotemiao.artemis.system.app.query.role.FindSystemRoleByIdQryExe;
import com.aotemiao.artemis.system.app.query.role.ListRoleDepartmentsQry;
import com.aotemiao.artemis.system.app.query.role.ListRoleDepartmentsQryExe;
import com.aotemiao.artemis.system.app.query.role.SystemRolePageQry;
import com.aotemiao.artemis.system.app.query.role.SystemRolePageQryExe;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import com.aotemiao.artemis.system.domain.model.role.SystemRole;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 系统角色目录 REST API。 */
@RestController
@RequestMapping(SystemRoleController.BASE_PATH)
public class SystemRoleController {

    public static final String BASE_PATH = "/api/roles";

    private final CreateSystemRoleCmdExe createSystemRoleCmdExe;
    private final UpdateSystemRoleCmdExe updateSystemRoleCmdExe;
    private final FindSystemRoleByIdQryExe findSystemRoleByIdQryExe;
    private final SystemRolePageQryExe systemRolePageQryExe;
    private final ListRoleMenusQryExe listRoleMenusQryExe;
    private final ReplaceRoleMenusCmdExe replaceRoleMenusCmdExe;
    private final ListRoleDepartmentsQryExe listRoleDepartmentsQryExe;
    private final ReplaceRoleDataScopeCmdExe replaceRoleDataScopeCmdExe;

    public SystemRoleController(
            CreateSystemRoleCmdExe createSystemRoleCmdExe,
            UpdateSystemRoleCmdExe updateSystemRoleCmdExe,
            FindSystemRoleByIdQryExe findSystemRoleByIdQryExe,
            SystemRolePageQryExe systemRolePageQryExe,
            ListRoleMenusQryExe listRoleMenusQryExe,
            ReplaceRoleMenusCmdExe replaceRoleMenusCmdExe,
            ListRoleDepartmentsQryExe listRoleDepartmentsQryExe,
            ReplaceRoleDataScopeCmdExe replaceRoleDataScopeCmdExe) {
        this.createSystemRoleCmdExe = createSystemRoleCmdExe;
        this.updateSystemRoleCmdExe = updateSystemRoleCmdExe;
        this.findSystemRoleByIdQryExe = findSystemRoleByIdQryExe;
        this.systemRolePageQryExe = systemRolePageQryExe;
        this.listRoleMenusQryExe = listRoleMenusQryExe;
        this.replaceRoleMenusCmdExe = replaceRoleMenusCmdExe;
        this.listRoleDepartmentsQryExe = listRoleDepartmentsQryExe;
        this.replaceRoleDataScopeCmdExe = replaceRoleDataScopeCmdExe;
    }

    @OperLogRecord(title = "角色管理", businessType = "INSERT")
    @PostMapping
    public R<SystemRoleDTO> create(@Valid @RequestBody CreateSystemRoleRequest request) {
        SystemRole systemRole = createSystemRoleCmdExe.execute(
                new CreateSystemRoleCmd(request.roleKey(), request.roleName(), request.dataScope()));
        return R.ok(toDTO(systemRole));
    }

    @OperLogRecord(title = "角色管理", businessType = "UPDATE")
    @PutMapping("/{id}")
    public R<SystemRoleDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateSystemRoleRequest request) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemRole systemRole = updateSystemRoleCmdExe.execute(new UpdateSystemRoleCmd(
                id, request.roleKey(), request.roleName(), request.dataScope(), request.enabled()));
        return R.ok(toDTO(systemRole));
    }

    @GetMapping("/{id}")
    public R<SystemRoleDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemRole systemRole = findSystemRoleByIdQryExe
                .execute(new FindSystemRoleByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemRole not found: " + id));
        return R.ok(toDTO(systemRole));
    }

    @GetMapping
    public R<PageResult<SystemRoleDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<SystemRole> pageResult =
                systemRolePageQryExe.execute(new SystemRolePageQry(new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(this::toDTO).toList(),
                pageResult.totalPages()));
    }

    @GetMapping("/{roleId}/menus")
    public R<List<SystemMenuDTO>> listMenus(@PathVariable Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid roleId: " + roleId);
        }
        return R.ok(listRoleMenusQryExe.execute(new ListRoleMenusQry(roleId)).stream()
                .map(this::toMenuDTO)
                .toList());
    }

    @OperLogRecord(title = "角色管理", businessType = "GRANT")
    @PutMapping("/{roleId}/menus")
    public R<List<SystemMenuDTO>> replaceMenus(
            @PathVariable Long roleId, @Valid @RequestBody ReplaceRoleMenusRequest request) {
        if (roleId == null || roleId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid roleId: " + roleId);
        }
        return R.ok(replaceRoleMenusCmdExe.execute(new ReplaceRoleMenusCmd(roleId, request.menuIds())).stream()
                .map(this::toMenuDTO)
                .toList());
    }

    @GetMapping("/{roleId}/departments")
    public R<List<Long>> listDepartments(@PathVariable Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid roleId: " + roleId);
        }
        return R.ok(listRoleDepartmentsQryExe.execute(new ListRoleDepartmentsQry(roleId)));
    }

    @OperLogRecord(title = "角色管理", businessType = "GRANT")
    @PutMapping("/{roleId}/data-scope")
    public R<List<Long>> replaceDataScope(
            @PathVariable Long roleId, @Valid @RequestBody ReplaceRoleDataScopeRequest request) {
        if (roleId == null || roleId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid roleId: " + roleId);
        }
        return R.ok(replaceRoleDataScopeCmdExe.execute(
                new ReplaceRoleDataScopeCmd(roleId, request.dataScope(), request.departmentIds())));
    }

    private SystemRoleDTO toDTO(SystemRole systemRole) {
        return new SystemRoleDTO(
                systemRole.getId(),
                systemRole.getRoleKey(),
                systemRole.getRoleName(),
                systemRole.getDataScope(),
                systemRole.isEnabled());
    }

    private SystemMenuDTO toMenuDTO(SystemMenu systemMenu) {
        return new SystemMenuDTO(
                systemMenu.getId(),
                systemMenu.getParentId(),
                systemMenu.getMenuType(),
                systemMenu.getMenuName(),
                systemMenu.getSortOrder(),
                systemMenu.getPath(),
                systemMenu.getComponent(),
                systemMenu.getQueryParam(),
                systemMenu.isExternalLink(),
                systemMenu.isCacheable(),
                systemMenu.getPermissionCode(),
                systemMenu.getIcon(),
                systemMenu.isVisible(),
                systemMenu.isEnabled(),
                systemMenu.getRemarks());
    }
}
