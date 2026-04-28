package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.SystemClientDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemClientRequest;
import com.aotemiao.artemis.system.app.command.client.CreateSystemClientCmd;
import com.aotemiao.artemis.system.app.command.client.CreateSystemClientCmdExe;
import com.aotemiao.artemis.system.app.command.client.DeleteSystemClientCmd;
import com.aotemiao.artemis.system.app.command.client.DeleteSystemClientCmdExe;
import com.aotemiao.artemis.system.app.command.client.UpdateSystemClientCmd;
import com.aotemiao.artemis.system.app.command.client.UpdateSystemClientCmdExe;
import com.aotemiao.artemis.system.app.query.client.FindSystemClientByIdQry;
import com.aotemiao.artemis.system.app.query.client.FindSystemClientByIdQryExe;
import com.aotemiao.artemis.system.app.query.client.SystemClientPageQry;
import com.aotemiao.artemis.system.app.query.client.SystemClientPageQryExe;
import com.aotemiao.artemis.system.domain.model.client.SystemClient;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 系统客户端 REST API。 */
@RestController
@RequestMapping(SystemClientController.BASE_PATH)
public class SystemClientController {

    public static final String BASE_PATH = "/api/clients";

    private final CreateSystemClientCmdExe createSystemClientCmdExe;
    private final UpdateSystemClientCmdExe updateSystemClientCmdExe;
    private final DeleteSystemClientCmdExe deleteSystemClientCmdExe;
    private final FindSystemClientByIdQryExe findSystemClientByIdQryExe;
    private final SystemClientPageQryExe systemClientPageQryExe;

    public SystemClientController(
            CreateSystemClientCmdExe createSystemClientCmdExe,
            UpdateSystemClientCmdExe updateSystemClientCmdExe,
            DeleteSystemClientCmdExe deleteSystemClientCmdExe,
            FindSystemClientByIdQryExe findSystemClientByIdQryExe,
            SystemClientPageQryExe systemClientPageQryExe) {
        this.createSystemClientCmdExe = createSystemClientCmdExe;
        this.updateSystemClientCmdExe = updateSystemClientCmdExe;
        this.deleteSystemClientCmdExe = deleteSystemClientCmdExe;
        this.findSystemClientByIdQryExe = findSystemClientByIdQryExe;
        this.systemClientPageQryExe = systemClientPageQryExe;
    }

    @PostMapping
    public R<SystemClientDTO> create(@Valid @RequestBody SystemClientRequest request) {
        SystemClient client = createSystemClientCmdExe.execute(new CreateSystemClientCmd(
                request.clientId(),
                request.clientKey(),
                request.clientSecret(),
                request.grantTypes(),
                request.deviceType(),
                request.activeTimeoutSeconds(),
                request.fixedTimeoutSeconds(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(client));
    }

    @PutMapping("/{id}")
    public R<SystemClientDTO> update(@PathVariable Long id, @Valid @RequestBody SystemClientRequest request) {
        SystemClient client = updateSystemClientCmdExe.execute(new UpdateSystemClientCmd(
                id,
                request.clientId(),
                request.clientKey(),
                request.clientSecret(),
                request.grantTypes(),
                request.deviceType(),
                request.activeTimeoutSeconds(),
                request.fixedTimeoutSeconds(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(client));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteSystemClientCmdExe.execute(new DeleteSystemClientCmd(id));
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<SystemClientDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemClient client = findSystemClientByIdQryExe
                .execute(new FindSystemClientByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Client not found: " + id));
        return R.ok(toDTO(client));
    }

    @GetMapping
    public R<PageResult<SystemClientDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<SystemClient> pr =
                systemClientPageQryExe.execute(new SystemClientPageQry(new PageRequest(page, size)));
        return R.ok(
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages()));
    }

    private SystemClientDTO toDTO(SystemClient client) {
        return new SystemClientDTO(
                client.getId(),
                client.getClientId(),
                client.getClientKey(),
                client.getClientSecret(),
                client.getGrantTypes(),
                client.getDeviceType(),
                client.getActiveTimeoutSeconds(),
                client.getFixedTimeoutSeconds(),
                client.getStatus(),
                client.getRemarks());
    }
}
