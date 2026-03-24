package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.CreateSystemUserRequest;
import com.aotemiao.artemis.system.adapter.web.dto.ReplaceUserRolesRequest;
import com.aotemiao.artemis.system.adapter.web.dto.SystemRoleDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemUserDTO;
import com.aotemiao.artemis.system.adapter.web.dto.UpdateSystemUserRequest;
import com.aotemiao.artemis.system.app.command.CreateSystemUserCmd;
import com.aotemiao.artemis.system.app.command.CreateSystemUserCmdExe;
import com.aotemiao.artemis.system.app.command.ReplaceUserRolesCmd;
import com.aotemiao.artemis.system.app.command.ReplaceUserRolesCmdExe;
import com.aotemiao.artemis.system.app.command.UpdateSystemUserCmd;
import com.aotemiao.artemis.system.app.command.UpdateSystemUserCmdExe;
import com.aotemiao.artemis.system.app.query.FindSystemUserByIdQry;
import com.aotemiao.artemis.system.app.query.FindSystemUserByIdQryExe;
import com.aotemiao.artemis.system.app.query.ListUserRolesQry;
import com.aotemiao.artemis.system.app.query.ListUserRolesQryExe;
import com.aotemiao.artemis.system.app.query.SystemUserPageQry;
import com.aotemiao.artemis.system.app.query.SystemUserPageQryExe;
import com.aotemiao.artemis.system.domain.model.SystemRole;
import com.aotemiao.artemis.system.domain.model.SystemUser;
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

/** 系统用户目录 REST API。 */
@RestController
@RequestMapping(SystemUserController.BASE_PATH)
public class SystemUserController {

    public static final String BASE_PATH = "/api/users";

    private final CreateSystemUserCmdExe createSystemUserCmdExe;
    private final UpdateSystemUserCmdExe updateSystemUserCmdExe;
    private final FindSystemUserByIdQryExe findSystemUserByIdQryExe;
    private final SystemUserPageQryExe systemUserPageQryExe;
    private final ListUserRolesQryExe listUserRolesQryExe;
    private final ReplaceUserRolesCmdExe replaceUserRolesCmdExe;

    public SystemUserController(
            CreateSystemUserCmdExe createSystemUserCmdExe,
            UpdateSystemUserCmdExe updateSystemUserCmdExe,
            FindSystemUserByIdQryExe findSystemUserByIdQryExe,
            SystemUserPageQryExe systemUserPageQryExe,
            ListUserRolesQryExe listUserRolesQryExe,
            ReplaceUserRolesCmdExe replaceUserRolesCmdExe) {
        this.createSystemUserCmdExe = createSystemUserCmdExe;
        this.updateSystemUserCmdExe = updateSystemUserCmdExe;
        this.findSystemUserByIdQryExe = findSystemUserByIdQryExe;
        this.systemUserPageQryExe = systemUserPageQryExe;
        this.listUserRolesQryExe = listUserRolesQryExe;
        this.replaceUserRolesCmdExe = replaceUserRolesCmdExe;
    }

    @PostMapping
    public R<SystemUserDTO> create(@Valid @RequestBody CreateSystemUserRequest request) {
        SystemUser systemUser = createSystemUserCmdExe.execute(
                new CreateSystemUserCmd(request.username(), request.displayName(), request.password()));
        return R.ok(toDTO(systemUser));
    }

    @PutMapping("/{id}")
    public R<SystemUserDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateSystemUserRequest request) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemUser systemUser = updateSystemUserCmdExe.execute(
                new UpdateSystemUserCmd(id, request.displayName(), request.password(), request.enabled()));
        return R.ok(toDTO(systemUser));
    }

    @GetMapping("/{id}")
    public R<SystemUserDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemUser systemUser = findSystemUserByIdQryExe
                .execute(new FindSystemUserByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemUser not found: " + id));
        return R.ok(toDTO(systemUser));
    }

    @GetMapping
    public R<PageResult<SystemUserDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<SystemUser> pageResult =
                systemUserPageQryExe.execute(new SystemUserPageQry(new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(this::toDTO).toList(),
                pageResult.totalPages()));
    }

    @GetMapping("/{userId}/roles")
    public R<List<SystemRoleDTO>> listRoles(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid userId: " + userId);
        }
        return R.ok(listUserRolesQryExe.execute(new ListUserRolesQry(userId)).stream()
                .map(this::toRoleDTO)
                .toList());
    }

    @PutMapping("/{userId}/roles")
    public R<List<SystemRoleDTO>> replaceRoles(
            @PathVariable Long userId, @Valid @RequestBody ReplaceUserRolesRequest request) {
        if (userId == null || userId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid userId: " + userId);
        }
        return R.ok(replaceUserRolesCmdExe.execute(new ReplaceUserRolesCmd(userId, request.roleIds())).stream()
                .map(this::toRoleDTO)
                .toList());
    }

    private SystemUserDTO toDTO(SystemUser systemUser) {
        return new SystemUserDTO(
                systemUser.getId(), systemUser.getUsername(), systemUser.getDisplayName(), systemUser.isEnabled());
    }

    private SystemRoleDTO toRoleDTO(SystemRole systemRole) {
        return new SystemRoleDTO(
                systemRole.getId(), systemRole.getRoleKey(), systemRole.getRoleName(), systemRole.isEnabled());
    }
}
