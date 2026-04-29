package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.CreateSystemMenuRequest;
import com.aotemiao.artemis.system.adapter.web.dto.SystemMenuDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemMenuRouteDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemMenuTreeDTO;
import com.aotemiao.artemis.system.adapter.web.dto.UpdateSystemMenuRequest;
import com.aotemiao.artemis.system.app.command.menu.CreateSystemMenuCmd;
import com.aotemiao.artemis.system.app.command.menu.CreateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.command.menu.DeleteSystemMenuCmd;
import com.aotemiao.artemis.system.app.command.menu.DeleteSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.command.menu.UpdateSystemMenuCmd;
import com.aotemiao.artemis.system.app.command.menu.UpdateSystemMenuCmdExe;
import com.aotemiao.artemis.system.app.query.menu.FindSystemMenuByIdQry;
import com.aotemiao.artemis.system.app.query.menu.FindSystemMenuByIdQryExe;
import com.aotemiao.artemis.system.app.query.menu.ListRoleMenusQry;
import com.aotemiao.artemis.system.app.query.menu.ListRoleMenusQryExe;
import com.aotemiao.artemis.system.app.query.menu.ListSystemMenusQry;
import com.aotemiao.artemis.system.app.query.menu.ListSystemMenusQryExe;
import com.aotemiao.artemis.system.app.query.menu.ListUserMenuRoutesQry;
import com.aotemiao.artemis.system.app.query.menu.ListUserMenuRoutesQryExe;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantPackageByIdQry;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantPackageByIdQryExe;
import com.aotemiao.artemis.system.domain.model.menu.SystemMenu;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final DeleteSystemMenuCmdExe deleteSystemMenuCmdExe;
    private final FindSystemMenuByIdQryExe findSystemMenuByIdQryExe;
    private final ListSystemMenusQryExe listSystemMenusQryExe;
    private final ListRoleMenusQryExe listRoleMenusQryExe;
    private final ListUserMenuRoutesQryExe listUserMenuRoutesQryExe;
    private final FindTenantPackageByIdQryExe findTenantPackageByIdQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this controller does not expose them.")
    public SystemMenuController(
            CreateSystemMenuCmdExe createSystemMenuCmdExe,
            UpdateSystemMenuCmdExe updateSystemMenuCmdExe,
            DeleteSystemMenuCmdExe deleteSystemMenuCmdExe,
            FindSystemMenuByIdQryExe findSystemMenuByIdQryExe,
            ListSystemMenusQryExe listSystemMenusQryExe,
            ListRoleMenusQryExe listRoleMenusQryExe,
            ListUserMenuRoutesQryExe listUserMenuRoutesQryExe,
            FindTenantPackageByIdQryExe findTenantPackageByIdQryExe) {
        this.createSystemMenuCmdExe = createSystemMenuCmdExe;
        this.updateSystemMenuCmdExe = updateSystemMenuCmdExe;
        this.deleteSystemMenuCmdExe = deleteSystemMenuCmdExe;
        this.findSystemMenuByIdQryExe = findSystemMenuByIdQryExe;
        this.listSystemMenusQryExe = listSystemMenusQryExe;
        this.listRoleMenusQryExe = listRoleMenusQryExe;
        this.listUserMenuRoutesQryExe = listUserMenuRoutesQryExe;
        this.findTenantPackageByIdQryExe = findTenantPackageByIdQryExe;
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
                request.queryParam(),
                request.externalLink(),
                request.cacheable(),
                request.permissionCode(),
                request.icon(),
                request.visible(),
                request.remarks()));
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
                request.queryParam(),
                request.externalLink(),
                request.cacheable(),
                request.permissionCode(),
                request.icon(),
                request.visible(),
                request.enabled(),
                request.remarks()));
        return R.ok(toDTO(systemMenu));
    }

    @OperLogRecord(title = "菜单管理", businessType = "DELETE")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        deleteSystemMenuCmdExe.execute(new DeleteSystemMenuCmd(id));
        return R.ok(true);
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

    @GetMapping("/tree")
    public R<List<SystemMenuTreeDTO>> tree() {
        return R.ok(toTree(listSystemMenusQryExe.execute(new ListSystemMenusQry()), Set.of()));
    }

    @GetMapping("/tree/roles/{roleId}")
    public R<List<SystemMenuTreeDTO>> roleTree(@PathVariable Long roleId) {
        if (roleId == null || roleId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid roleId: " + roleId);
        }
        Set<Long> checkedIds = listRoleMenusQryExe.execute(new ListRoleMenusQry(roleId)).stream()
                .map(SystemMenu::getId)
                .collect(HashSet::new, Set::add, Set::addAll);
        return R.ok(toTree(listSystemMenusQryExe.execute(new ListSystemMenusQry()), checkedIds));
    }

    @GetMapping("/tree/tenant-packages/{packageId}")
    public R<List<SystemMenuTreeDTO>> tenantPackageTree(@PathVariable Long packageId) {
        if (packageId == null || packageId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid packageId: " + packageId);
        }
        Set<Long> checkedIds = findTenantPackageByIdQryExe
                .execute(new FindTenantPackageByIdQry(packageId))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "TenantPackage not found: " + packageId))
                .getMenuIds()
                .stream()
                .collect(HashSet::new, Set::add, Set::addAll);
        return R.ok(toTree(listSystemMenusQryExe.execute(new ListSystemMenusQry()), checkedIds));
    }

    @GetMapping("/routes/users/{userId}")
    public R<List<SystemMenuRouteDTO>> userRoutes(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid userId: " + userId);
        }
        return R.ok(toRouteTree(listUserMenuRoutesQryExe.execute(new ListUserMenuRoutesQry(userId))));
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
                systemMenu.getQueryParam(),
                systemMenu.isExternalLink(),
                systemMenu.isCacheable(),
                systemMenu.getPermissionCode(),
                systemMenu.getIcon(),
                systemMenu.isVisible(),
                systemMenu.isEnabled(),
                systemMenu.getRemarks());
    }

    private List<SystemMenuTreeDTO> toTree(List<SystemMenu> menus, Set<Long> checkedIds) {
        Map<Long, List<SystemMenu>> childrenByParent = groupByParent(menus);
        return toTreeChildren(childrenByParent, 0L, checkedIds);
    }

    private List<SystemMenuTreeDTO> toTreeChildren(
            Map<Long, List<SystemMenu>> childrenByParent, Long parentId, Set<Long> checkedIds) {
        return childrenByParent.getOrDefault(parentId, List.of()).stream()
                .map(menu -> new SystemMenuTreeDTO(
                        toDTO(menu),
                        checkedIds.contains(menu.getId()),
                        toTreeChildren(childrenByParent, menu.getId(), checkedIds)))
                .toList();
    }

    private List<SystemMenuRouteDTO> toRouteTree(List<SystemMenu> menus) {
        Map<Long, List<SystemMenu>> childrenByParent = groupByParent(menus);
        return toRouteChildren(childrenByParent, 0L);
    }

    private List<SystemMenuRouteDTO> toRouteChildren(Map<Long, List<SystemMenu>> childrenByParent, Long parentId) {
        return childrenByParent.getOrDefault(parentId, List.of()).stream()
                .map(menu -> new SystemMenuRouteDTO(
                        menu.getId(),
                        menu.getParentId(),
                        menu.getMenuName(),
                        menu.getPath(),
                        menu.getComponent(),
                        menu.getQueryParam(),
                        menu.isExternalLink(),
                        menu.isCacheable(),
                        menu.isVisible(),
                        menu.getPermissionCode(),
                        menu.getIcon(),
                        toRouteChildren(childrenByParent, menu.getId())))
                .toList();
    }

    private Map<Long, List<SystemMenu>> groupByParent(List<SystemMenu> menus) {
        Map<Long, List<SystemMenu>> grouped = new LinkedHashMap<>();
        menus.forEach(menu -> grouped.computeIfAbsent(menu.getParentId(), ignored -> new ArrayList<>())
                .add(menu));
        return grouped;
    }
}
