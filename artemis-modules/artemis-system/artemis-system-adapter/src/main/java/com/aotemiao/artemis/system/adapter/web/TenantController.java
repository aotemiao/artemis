package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.TenantDTO;
import com.aotemiao.artemis.system.adapter.web.dto.TenantRequest;
import com.aotemiao.artemis.system.adapter.web.dto.UpdateTenantStatusRequest;
import com.aotemiao.artemis.system.app.command.tenant.CreateTenantCmd;
import com.aotemiao.artemis.system.app.command.tenant.CreateTenantCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.DeleteTenantCmd;
import com.aotemiao.artemis.system.app.command.tenant.DeleteTenantCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantCmdExe;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantStatusCmd;
import com.aotemiao.artemis.system.app.command.tenant.UpdateTenantStatusCmdExe;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantByIdQry;
import com.aotemiao.artemis.system.app.query.tenant.FindTenantByIdQryExe;
import com.aotemiao.artemis.system.app.query.tenant.ListEnabledTenantsQry;
import com.aotemiao.artemis.system.app.query.tenant.ListEnabledTenantsQryExe;
import com.aotemiao.artemis.system.app.query.tenant.TenantPageQry;
import com.aotemiao.artemis.system.app.query.tenant.TenantPageQryExe;
import com.aotemiao.artemis.system.domain.model.tenant.Tenant;
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

@RestController
@RequestMapping(TenantController.BASE_PATH)
public class TenantController {

    public static final String BASE_PATH = "/api/tenants";

    private final CreateTenantCmdExe createTenantCmdExe;
    private final UpdateTenantCmdExe updateTenantCmdExe;
    private final UpdateTenantStatusCmdExe updateTenantStatusCmdExe;
    private final DeleteTenantCmdExe deleteTenantCmdExe;
    private final FindTenantByIdQryExe findTenantByIdQryExe;
    private final TenantPageQryExe tenantPageQryExe;
    private final ListEnabledTenantsQryExe listEnabledTenantsQryExe;

    public TenantController(
            CreateTenantCmdExe createTenantCmdExe,
            UpdateTenantCmdExe updateTenantCmdExe,
            UpdateTenantStatusCmdExe updateTenantStatusCmdExe,
            DeleteTenantCmdExe deleteTenantCmdExe,
            FindTenantByIdQryExe findTenantByIdQryExe,
            TenantPageQryExe tenantPageQryExe,
            ListEnabledTenantsQryExe listEnabledTenantsQryExe) {
        this.createTenantCmdExe = createTenantCmdExe;
        this.updateTenantCmdExe = updateTenantCmdExe;
        this.updateTenantStatusCmdExe = updateTenantStatusCmdExe;
        this.deleteTenantCmdExe = deleteTenantCmdExe;
        this.findTenantByIdQryExe = findTenantByIdQryExe;
        this.tenantPageQryExe = tenantPageQryExe;
        this.listEnabledTenantsQryExe = listEnabledTenantsQryExe;
    }

    @OperLogRecord(title = "租户管理", businessType = "INSERT")
    @PostMapping
    public R<TenantDTO> create(@Valid @RequestBody TenantRequest request) {
        Tenant tenant = createTenantCmdExe.execute(new CreateTenantCmd(
                request.companyName(),
                request.contactName(),
                request.contactPhone(),
                request.socialCreditCode(),
                request.address(),
                request.domain(),
                request.intro(),
                request.packageId(),
                request.expireTime(),
                request.userLimit(),
                request.remarks()));
        return R.ok(toDTO(tenant));
    }

    @OperLogRecord(title = "租户管理", businessType = "UPDATE")
    @PutMapping("/{id}")
    public R<TenantDTO> update(@PathVariable Long id, @Valid @RequestBody TenantRequest request) {
        Tenant tenant = updateTenantCmdExe.execute(new UpdateTenantCmd(
                id,
                request.companyName(),
                request.contactName(),
                request.contactPhone(),
                request.socialCreditCode(),
                request.address(),
                request.domain(),
                request.intro(),
                request.expireTime(),
                request.userLimit(),
                request.remarks()));
        return R.ok(toDTO(tenant));
    }

    @OperLogRecord(title = "租户管理", businessType = "UPDATE")
    @PutMapping("/{id}/status")
    public R<TenantDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateTenantStatusRequest request) {
        Tenant tenant = updateTenantStatusCmdExe.execute(new UpdateTenantStatusCmd(id, request.status()));
        return R.ok(toDTO(tenant));
    }

    @OperLogRecord(title = "租户管理", businessType = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteTenantCmdExe.execute(new DeleteTenantCmd(id));
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<TenantDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        Tenant tenant = findTenantByIdQryExe
                .execute(new FindTenantByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Tenant not found: " + id));
        return R.ok(toDTO(tenant));
    }

    @GetMapping
    public R<PageResult<TenantDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<Tenant> pr = tenantPageQryExe.execute(new TenantPageQry(new PageRequest(page, size)));
        return R.ok(
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages()));
    }

    @GetMapping("/select")
    public R<List<TenantDTO>> select() {
        return R.ok(listEnabledTenantsQryExe.execute(new ListEnabledTenantsQry()).stream()
                .map(this::toDTO)
                .toList());
    }

    private TenantDTO toDTO(Tenant tenant) {
        return new TenantDTO(
                tenant.getId(),
                tenant.getTenantNo(),
                tenant.getCompanyName(),
                tenant.getContactName(),
                tenant.getContactPhone(),
                tenant.getSocialCreditCode(),
                tenant.getAddress(),
                tenant.getDomain(),
                tenant.getIntro(),
                tenant.getPackageId(),
                tenant.getExpireTime(),
                tenant.getUserLimit(),
                tenant.getStatus(),
                tenant.getRemarks());
    }
}
