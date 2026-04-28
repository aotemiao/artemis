package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.SystemConfigDTO;
import com.aotemiao.artemis.system.adapter.web.dto.SystemConfigRequest;
import com.aotemiao.artemis.system.adapter.web.dto.UpdateSystemConfigValueRequest;
import com.aotemiao.artemis.system.app.command.config.CreateSystemConfigCmd;
import com.aotemiao.artemis.system.app.command.config.CreateSystemConfigCmdExe;
import com.aotemiao.artemis.system.app.command.config.DeleteSystemConfigCmd;
import com.aotemiao.artemis.system.app.command.config.DeleteSystemConfigCmdExe;
import com.aotemiao.artemis.system.app.command.config.RefreshSystemConfigCacheCmd;
import com.aotemiao.artemis.system.app.command.config.RefreshSystemConfigCacheCmdExe;
import com.aotemiao.artemis.system.app.command.config.UpdateSystemConfigCmd;
import com.aotemiao.artemis.system.app.command.config.UpdateSystemConfigCmdExe;
import com.aotemiao.artemis.system.app.command.config.UpdateSystemConfigValueCmd;
import com.aotemiao.artemis.system.app.command.config.UpdateSystemConfigValueCmdExe;
import com.aotemiao.artemis.system.app.query.config.FindSystemConfigByIdQry;
import com.aotemiao.artemis.system.app.query.config.FindSystemConfigByIdQryExe;
import com.aotemiao.artemis.system.app.query.config.GetSystemConfigValueQry;
import com.aotemiao.artemis.system.app.query.config.GetSystemConfigValueQryExe;
import com.aotemiao.artemis.system.app.query.config.SystemConfigPageQry;
import com.aotemiao.artemis.system.app.query.config.SystemConfigPageQryExe;
import com.aotemiao.artemis.system.domain.model.config.SystemConfig;
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

/** 系统参数配置 REST API。 */
@RestController
@RequestMapping(SystemConfigController.BASE_PATH)
public class SystemConfigController {

    public static final String BASE_PATH = "/api/configs";

    private final CreateSystemConfigCmdExe createSystemConfigCmdExe;
    private final UpdateSystemConfigCmdExe updateSystemConfigCmdExe;
    private final UpdateSystemConfigValueCmdExe updateSystemConfigValueCmdExe;
    private final DeleteSystemConfigCmdExe deleteSystemConfigCmdExe;
    private final RefreshSystemConfigCacheCmdExe refreshSystemConfigCacheCmdExe;
    private final FindSystemConfigByIdQryExe findSystemConfigByIdQryExe;
    private final SystemConfigPageQryExe systemConfigPageQryExe;
    private final GetSystemConfigValueQryExe getSystemConfigValueQryExe;

    public SystemConfigController(
            CreateSystemConfigCmdExe createSystemConfigCmdExe,
            UpdateSystemConfigCmdExe updateSystemConfigCmdExe,
            UpdateSystemConfigValueCmdExe updateSystemConfigValueCmdExe,
            DeleteSystemConfigCmdExe deleteSystemConfigCmdExe,
            RefreshSystemConfigCacheCmdExe refreshSystemConfigCacheCmdExe,
            FindSystemConfigByIdQryExe findSystemConfigByIdQryExe,
            SystemConfigPageQryExe systemConfigPageQryExe,
            GetSystemConfigValueQryExe getSystemConfigValueQryExe) {
        this.createSystemConfigCmdExe = createSystemConfigCmdExe;
        this.updateSystemConfigCmdExe = updateSystemConfigCmdExe;
        this.updateSystemConfigValueCmdExe = updateSystemConfigValueCmdExe;
        this.deleteSystemConfigCmdExe = deleteSystemConfigCmdExe;
        this.refreshSystemConfigCacheCmdExe = refreshSystemConfigCacheCmdExe;
        this.findSystemConfigByIdQryExe = findSystemConfigByIdQryExe;
        this.systemConfigPageQryExe = systemConfigPageQryExe;
        this.getSystemConfigValueQryExe = getSystemConfigValueQryExe;
    }

    @OperLogRecord(title = "参数配置", businessType = "INSERT")
    @PostMapping
    public R<SystemConfigDTO> create(@Valid @RequestBody SystemConfigRequest request) {
        SystemConfig systemConfig = createSystemConfigCmdExe.execute(new CreateSystemConfigCmd(
                request.configName(),
                request.configKey(),
                request.configValue(),
                request.systemBuiltIn(),
                request.remarks()));
        return R.ok(toDTO(systemConfig));
    }

    @OperLogRecord(title = "参数配置", businessType = "UPDATE")
    @PutMapping("/{id}")
    public R<SystemConfigDTO> update(@PathVariable Long id, @Valid @RequestBody SystemConfigRequest request) {
        SystemConfig systemConfig = updateSystemConfigCmdExe.execute(new UpdateSystemConfigCmd(
                id,
                request.configName(),
                request.configKey(),
                request.configValue(),
                request.systemBuiltIn(),
                request.remarks()));
        return R.ok(toDTO(systemConfig));
    }

    @OperLogRecord(title = "参数配置", businessType = "UPDATE")
    @PutMapping("/key/{configKey}")
    public R<SystemConfigDTO> updateValueByKey(
            @PathVariable String configKey, @Valid @RequestBody UpdateSystemConfigValueRequest request) {
        SystemConfig systemConfig =
                updateSystemConfigValueCmdExe.execute(new UpdateSystemConfigValueCmd(configKey, request.configValue()));
        return R.ok(toDTO(systemConfig));
    }

    @OperLogRecord(title = "参数配置", businessType = "DELETE")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteSystemConfigCmdExe.execute(new DeleteSystemConfigCmd(id));
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<SystemConfigDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemConfig systemConfig = findSystemConfigByIdQryExe
                .execute(new FindSystemConfigByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemConfig not found: " + id));
        return R.ok(toDTO(systemConfig));
    }

    @GetMapping
    public R<PageResult<SystemConfigDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<SystemConfig> pr =
                systemConfigPageQryExe.execute(new SystemConfigPageQry(new PageRequest(page, size)));
        PageResult<SystemConfigDTO> dtoPage =
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages());
        return R.ok(dtoPage);
    }

    @GetMapping("/key/{configKey}")
    public R<String> getValueByKey(@PathVariable String configKey) {
        String value = getSystemConfigValueQryExe
                .execute(new GetSystemConfigValueQry(configKey))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemConfig not found: " + configKey));
        return R.ok(value);
    }

    @OperLogRecord(title = "参数配置", businessType = "CLEAN")
    @PostMapping("/cache/refresh")
    public R<Void> refreshCache() {
        refreshSystemConfigCacheCmdExe.execute(new RefreshSystemConfigCacheCmd());
        return R.ok();
    }

    private SystemConfigDTO toDTO(SystemConfig systemConfig) {
        return new SystemConfigDTO(
                systemConfig.getId(),
                systemConfig.getConfigName(),
                systemConfig.getConfigKey(),
                systemConfig.getConfigValue(),
                systemConfig.isSystemBuiltIn(),
                systemConfig.getRemarks());
    }
}
