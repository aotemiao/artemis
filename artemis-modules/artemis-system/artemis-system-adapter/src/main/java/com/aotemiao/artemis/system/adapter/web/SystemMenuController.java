package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.CreateSystemMenuRequest;
import com.aotemiao.artemis.system.adapter.web.dto.SystemMenuDTO;
import com.aotemiao.artemis.system.adapter.web.dto.UpdateSystemMenuRequest;
import com.aotemiao.artemis.system.app.command.CreateSystemMenuCmd;
import com.aotemiao.artemis.system.app.command.CreateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.command.UpdateSystemMenuCmd;
import com.aotemiao.artemis.system.app.command.UpdateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.query.FindSystemMenuByIdQry;
import com.aotemiao.artemis.system.app.query.FindSystemMenuByIdQryExe;
import com.aotemiao.artemis.system.app.query.ListSystemMenusQry;
import com.aotemiao.artemis.system.app.query.ListSystemMenusQryExe;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 系统菜单与权限点 REST API。 */
@RestController
@RequestMapping(SystemMenuController.BASE_PATH)
public class SystemMenuController {

    public static final String BASE_PATH = "/api/menus";

    private final CreateSystemMenuCmdExe createSystemMenuCmdExe;
    private final UpdateSystemMenuCmdExe updateSystemMenuCmdExe;
    private final FindSystemMenuByIdQryExe findSystemMenuByIdQryExe;
    private final ListSystemMenusQryExe listSystemMenusQryExe;

    public SystemMenuController(
            CreateSystemMenuCmdExe createSystemMenuCmdExe,
            UpdateSystemMenuCmdExe updateSystemMenuCmdExe,
            FindSystemMenuByIdQryExe findSystemMenuByIdQryExe,
            ListSystemMenusQryExe listSystemMenusQryExe) {
        this.createSystemMenuCmdExe = createSystemMenuCmdExe;
        this.updateSystemMenuCmdExe = updateSystemMenuCmdExe;
        this.findSystemMenuByIdQryExe = findSystemMenuByIdQryExe;
        this.listSystemMenusQryExe = listSystemMenusQryExe;
    }

    @PostMapping
    public R<SystemMenuDTO> create(@Valid @RequestBody CreateSystemMenuRequest request) {
        SystemMenu systemMenu = createSystemMenuCmdExe.execute(new CreateSystemMenuCmd(
                request.parentId(),
                request.menuType(),
                request.menuName(),
                request.sortOrder(),
                request.path(),
                request.component(),
                request.permissionCode(),
                request.visible()));
        return R.ok(toDTO(systemMenu));
    }

    @PutMapping("/{id}")
    public R<SystemMenuDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateSystemMenuRequest request) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemMenu systemMenu = updateSystemMenuCmdExe.execute(new UpdateSystemMenuCmd(
                id,
                request.parentId(),
                request.menuType(),
                request.menuName(),
                request.sortOrder(),
                request.path(),
                request.component(),
                request.permissionCode(),
                request.visible(),
                request.enabled()));
        return R.ok(toDTO(systemMenu));
    }

    @GetMapping("/{id}")
    public R<SystemMenuDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemMenu systemMenu = findSystemMenuByIdQryExe
                .execute(new FindSystemMenuByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemMenu not found: " + id));
        return R.ok(toDTO(systemMenu));
    }

    @GetMapping
    public R<List<SystemMenuDTO>> list() {
        return R.ok(listSystemMenusQryExe.execute(new ListSystemMenusQry()).stream()
                .map(this::toDTO)
                .toList());
    }

    SystemMenuDTO toDTO(SystemMenu systemMenu) {
        return new SystemMenuDTO(
                systemMenu.getId(),
                systemMenu.getParentId(),
                systemMenu.getMenuType(),
                systemMenu.getMenuName(),
                systemMenu.getSortOrder(),
                systemMenu.getPath(),
                systemMenu.getComponent(),
                systemMenu.getPermissionCode(),
                systemMenu.isVisible(),
                systemMenu.isEnabled());
    }
}
