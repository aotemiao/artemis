package com.aotemiao.artemis.system.adapter.web.tenant;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.audit.OperLogRecord;
import com.aotemiao.artemis.system.adapter.web.dto.tenant.TenantPackageDTO;
import com.aotemiao.artemis.system.adapter.web.dto.tenant.TenantPackageRequest;
import com.aotemiao.artemis.system.adapter.web.dto.tenant.UpdateTenantPackageStatusRequest;
import com.aotemiao.artemis.system.app.command.tenant.CreateTenantPackageCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.DeleteTenantPackageCmd;
import com.aotemiao.artemis.system.app.command.tenant.DeleteTenantPackageCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.TenantPackageCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageStatusCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantPackageStatusCmdExe;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantPackageByIdQry;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantPackageByIdQryExe;
import com.aotemiao.artemis.system.app.query.tenant.ListEnabledTenantPackagesQry;
import com.aotemiao.artemis.system.app.query.tenant.ListEnabledTenantPackagesQryExe;
import com.aotemiao.artemis.system.app.query.tenant.TenantPackagePageQry;
import com.aotemiao.artemis.system.app.query.tenant.TenantPackagePageQryExe;
import com.aotemiao.artemis.system.domain.model.tenant.TenantPackage;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 租户套餐 REST API。 */
@RestController
@RequestMapping(TenantPackageController.BASE_PATH)
public class TenantPackageController {

    public static final String BASE_PATH = "/api/tenant-packages";

    private final CreateTenantPackageCmdExe createTenantPackageCmdExe;
    private final UpdateTenantPackageCmdExe updateTenantPackageCmdExe;
    private final UpdateTenantPackageStatusCmdExe updateTenantPackageStatusCmdExe;
    private final DeleteTenantPackageCmdExe deleteTenantPackageCmdExe;
    private final FindTenantPackageByIdQryExe findTenantPackageByIdQryExe;
    private final TenantPackagePageQryExe tenantPackagePageQryExe;
    private final ListEnabledTenantPackagesQryExe listEnabledTenantPackagesQryExe;

    public TenantPackageController(
            CreateTenantPackageCmdExe createTenantPackageCmdExe,
            UpdateTenantPackageCmdExe updateTenantPackageCmdExe,
            UpdateTenantPackageStatusCmdExe updateTenantPackageStatusCmdExe,
            DeleteTenantPackageCmdExe deleteTenantPackageCmdExe,
            FindTenantPackageByIdQryExe findTenantPackageByIdQryExe,
            TenantPackagePageQryExe tenantPackagePageQryExe,
            ListEnabledTenantPackagesQryExe listEnabledTenantPackagesQryExe) {
        this.createTenantPackageCmdExe = createTenantPackageCmdExe;
        this.updateTenantPackageCmdExe = updateTenantPackageCmdExe;
        this.updateTenantPackageStatusCmdExe = updateTenantPackageStatusCmdExe;
        this.deleteTenantPackageCmdExe = deleteTenantPackageCmdExe;
        this.findTenantPackageByIdQryExe = findTenantPackageByIdQryExe;
        this.tenantPackagePageQryExe = tenantPackagePageQryExe;
        this.listEnabledTenantPackagesQryExe = listEnabledTenantPackagesQryExe;
    }

    @OperLogRecord(title = "租户套餐", businessType = "INSERT")
    @PostMapping
    public R<TenantPackageDTO> create(@Valid @RequestBody TenantPackageRequest request) {
        TenantPackage tenantPackage = createTenantPackageCmdExe.execute(new TenantPackageCmd(
                request.packageName(),
                request.menuCheckStrictly(),
                request.enabled(),
                request.remarks(),
                request.menuIds()));
        return R.ok(toDTO(tenantPackage));
    }

    @OperLogRecord(title = "租户套餐", businessType = "UPDATE")
    @PutMapping("/{id}")
    public R<TenantPackageDTO> update(@PathVariable Long id, @Valid @RequestBody TenantPackageRequest request) {
        TenantPackage tenantPackage = updateTenantPackageCmdExe.execute(new UpdateTenantPackageCmd(
                id,
                request.packageName(),
                request.menuCheckStrictly(),
                request.enabled(),
                request.remarks(),
                request.menuIds()));
        return R.ok(toDTO(tenantPackage));
    }

    @OperLogRecord(title = "租户套餐", businessType = "UPDATE")
    @PutMapping("/{id}/status")
    public R<TenantPackageDTO> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateTenantPackageStatusRequest request) {
        TenantPackage tenantPackage =
                updateTenantPackageStatusCmdExe.execute(new UpdateTenantPackageStatusCmd(id, request.enabled()));
        return R.ok(toDTO(tenantPackage));
    }

    @OperLogRecord(title = "租户套餐", businessType = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteTenantPackageCmdExe.execute(new DeleteTenantPackageCmd(id));
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<TenantPackageDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        TenantPackage tenantPackage = findTenantPackageByIdQryExe
                .execute(new FindTenantPackageByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "TenantPackage not found: " + id));
        return R.ok(toDTO(tenantPackage));
    }

    @GetMapping
    public R<PageResult<TenantPackageDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<TenantPackage> pr =
                tenantPackagePageQryExe.execute(new TenantPackagePageQry(new PageRequest(page, size)));
        return R.ok(
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages()));
    }

    @GetMapping("/select")
    public R<List<TenantPackageDTO>> select() {
        return R.ok(listEnabledTenantPackagesQryExe.execute(new ListEnabledTenantPackagesQry()).stream()
                .map(this::toDTO)
                .toList());
    }

    private TenantPackageDTO toDTO(TenantPackage tenantPackage) {
        return new TenantPackageDTO(
                tenantPackage.getId(),
                tenantPackage.getPackageName(),
                tenantPackage.isMenuCheckStrictly(),
                tenantPackage.isEnabled(),
                tenantPackage.getRemarks(),
                tenantPackage.getMenuIds());
    }
}
