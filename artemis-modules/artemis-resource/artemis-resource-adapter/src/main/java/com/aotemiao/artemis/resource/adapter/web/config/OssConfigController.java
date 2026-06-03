package com.aotemiao.artemis.resource.adapter.web.config;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.adapter.web.dto.config.OssConfigDTO;
import com.aotemiao.artemis.resource.adapter.web.dto.config.OssConfigRequest;
import com.aotemiao.artemis.resource.adapter.web.dto.config.OssConfigStatusRequest;
import com.aotemiao.artemis.resource.app.command.config.ChangeOssConfigStatusCmd;
import com.aotemiao.artemis.resource.app.command.config.ChangeOssConfigStatusCmdExe;
import com.aotemiao.artemis.resource.app.command.config.CreateOssConfigCmd;
import com.aotemiao.artemis.resource.app.command.config.CreateOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.command.config.DeleteOssConfigCmd;
import com.aotemiao.artemis.resource.app.command.config.DeleteOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.command.config.OssConfigPayload;
import com.aotemiao.artemis.resource.app.command.config.SetDefaultOssConfigCmd;
import com.aotemiao.artemis.resource.app.command.config.SetDefaultOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.command.config.UpdateOssConfigCmd;
import com.aotemiao.artemis.resource.app.command.config.UpdateOssConfigCmdExe;
import com.aotemiao.artemis.resource.app.query.config.FindOssConfigByIdQry;
import com.aotemiao.artemis.resource.app.query.config.FindOssConfigByIdQryExe;
import com.aotemiao.artemis.resource.app.query.config.OssConfigPageQry;
import com.aotemiao.artemis.resource.app.query.config.OssConfigPageQryExe;
import com.aotemiao.artemis.resource.domain.model.config.OssConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** OSS 配置 REST API。 */
@RestController
@RequestMapping(OssConfigController.BASE_PATH)
public class OssConfigController {

    public static final String BASE_PATH = "/api/resource/oss-configs";

    private final CreateOssConfigCmdExe createOssConfigCmdExe;
    private final UpdateOssConfigCmdExe updateOssConfigCmdExe;
    private final DeleteOssConfigCmdExe deleteOssConfigCmdExe;
    private final ChangeOssConfigStatusCmdExe changeOssConfigStatusCmdExe;
    private final SetDefaultOssConfigCmdExe setDefaultOssConfigCmdExe;
    private final FindOssConfigByIdQryExe findOssConfigByIdQryExe;
    private final OssConfigPageQryExe ossConfigPageQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this controller does not expose them.")
    public OssConfigController(
            CreateOssConfigCmdExe createOssConfigCmdExe,
            UpdateOssConfigCmdExe updateOssConfigCmdExe,
            DeleteOssConfigCmdExe deleteOssConfigCmdExe,
            ChangeOssConfigStatusCmdExe changeOssConfigStatusCmdExe,
            SetDefaultOssConfigCmdExe setDefaultOssConfigCmdExe,
            FindOssConfigByIdQryExe findOssConfigByIdQryExe,
            OssConfigPageQryExe ossConfigPageQryExe) {
        this.createOssConfigCmdExe = createOssConfigCmdExe;
        this.updateOssConfigCmdExe = updateOssConfigCmdExe;
        this.deleteOssConfigCmdExe = deleteOssConfigCmdExe;
        this.changeOssConfigStatusCmdExe = changeOssConfigStatusCmdExe;
        this.setDefaultOssConfigCmdExe = setDefaultOssConfigCmdExe;
        this.findOssConfigByIdQryExe = findOssConfigByIdQryExe;
        this.ossConfigPageQryExe = ossConfigPageQryExe;
    }

    @GetMapping
    public R<PageResult<OssConfigDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<OssConfig> pageResult =
                ossConfigPageQryExe.execute(new OssConfigPageQry(new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(this::toDTO).toList(),
                pageResult.totalPages()));
    }

    @GetMapping("/{id}")
    public R<OssConfigDTO> getById(@PathVariable Long id) {
        return R.ok(toDTO(findOssConfigByIdQryExe
                .execute(new FindOssConfigByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Oss config not found: " + id))));
    }

    @PostMapping
    public R<OssConfigDTO> create(@RequestBody OssConfigRequest request) {
        return R.ok(toDTO(createOssConfigCmdExe.execute(new CreateOssConfigCmd(toPayload(request)))));
    }

    @PutMapping("/{id}")
    public R<OssConfigDTO> update(@PathVariable Long id, @RequestBody OssConfigRequest request) {
        return R.ok(toDTO(updateOssConfigCmdExe.execute(new UpdateOssConfigCmd(id, toPayload(request)))));
    }

    @PutMapping("/{id}/status")
    public R<OssConfigDTO> changeStatus(@PathVariable Long id, @RequestBody OssConfigStatusRequest request) {
        return R.ok(toDTO(changeOssConfigStatusCmdExe.execute(new ChangeOssConfigStatusCmd(id, request.status()))));
    }

    @PutMapping("/{id}/default")
    public R<OssConfigDTO> setDefault(@PathVariable Long id) {
        return R.ok(toDTO(setDefaultOssConfigCmdExe.execute(new SetDefaultOssConfigCmd(id))));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        deleteOssConfigCmdExe.execute(new DeleteOssConfigCmd(id));
        return R.ok(true);
    }

    private OssConfigPayload toPayload(OssConfigRequest request) {
        if (request == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Oss config request must not be null");
        }
        return new OssConfigPayload(
                request.configKey(),
                request.accessKey(),
                request.secretKey(),
                request.bucket(),
                request.prefix(),
                request.endpoint(),
                request.customDomain(),
                request.httpsEnabled(),
                request.region(),
                request.accessPolicy(),
                request.status(),
                request.defaultFlag(),
                request.builtIn(),
                request.provider(),
                request.extJson());
    }

    private OssConfigDTO toDTO(OssConfig config) {
        return new OssConfigDTO(
                config.getId(),
                config.getConfigKey(),
                config.getAccessKey(),
                config.getSecretKey(),
                config.getBucket(),
                config.getPrefix(),
                config.getEndpoint(),
                config.getCustomDomain(),
                config.getHttpsEnabled(),
                config.getRegion(),
                config.getAccessPolicy(),
                config.getStatus(),
                config.getDefaultFlag(),
                config.getBuiltIn(),
                config.getProvider(),
                config.getExtJson());
    }
}
