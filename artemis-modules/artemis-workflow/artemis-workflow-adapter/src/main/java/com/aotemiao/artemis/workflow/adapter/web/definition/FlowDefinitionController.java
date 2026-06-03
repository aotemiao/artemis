package com.aotemiao.artemis.workflow.adapter.web.definition;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.adapter.web.dto.definition.FlowDefinitionCopyRequest;
import com.aotemiao.artemis.workflow.adapter.web.dto.definition.FlowDefinitionDTO;
import com.aotemiao.artemis.workflow.adapter.web.dto.definition.FlowDefinitionRequest;
import com.aotemiao.artemis.workflow.adapter.web.dto.definition.FlowDefinitionTenantSyncRequest;
import com.aotemiao.artemis.workflow.app.command.definition.ActivateFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.CancelPublishFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.ChangeFlowDefinitionStateCmd;
import com.aotemiao.artemis.workflow.app.command.definition.CopyFlowDefinitionCmd;
import com.aotemiao.artemis.workflow.app.command.definition.CopyFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.CreateFlowDefinitionCmd;
import com.aotemiao.artemis.workflow.app.command.definition.CreateFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.DeleteFlowDefinitionCmd;
import com.aotemiao.artemis.workflow.app.command.definition.DeleteFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.FlowDefinitionPayload;
import com.aotemiao.artemis.workflow.app.command.definition.PublishFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.SuspendFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.SyncFlowDefinitionTenantCmd;
import com.aotemiao.artemis.workflow.app.command.definition.SyncFlowDefinitionTenantCmdExe;
import com.aotemiao.artemis.workflow.app.command.definition.UpdateFlowDefinitionCmd;
import com.aotemiao.artemis.workflow.app.command.definition.UpdateFlowDefinitionCmdExe;
import com.aotemiao.artemis.workflow.app.query.definition.FindFlowDefinitionByIdQry;
import com.aotemiao.artemis.workflow.app.query.definition.FindFlowDefinitionByIdQryExe;
import com.aotemiao.artemis.workflow.app.query.definition.FlowDefinitionPageQry;
import com.aotemiao.artemis.workflow.app.query.definition.FlowDefinitionPageQryExe;
import com.aotemiao.artemis.workflow.app.query.definition.ListFlowDefinitionQry;
import com.aotemiao.artemis.workflow.app.query.definition.ListFlowDefinitionQryExe;
import com.aotemiao.artemis.workflow.app.query.definition.ListUnpublishedFlowDefinitionQry;
import com.aotemiao.artemis.workflow.app.query.definition.ListUnpublishedFlowDefinitionQryExe;
import com.aotemiao.artemis.workflow.domain.model.definition.FlowDefinition;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

/** 流程定义 REST API。 */
@RestController
@RequestMapping(FlowDefinitionController.BASE_PATH)
public class FlowDefinitionController {

    public static final String BASE_PATH = "/api/workflow/definitions";

    private final CreateFlowDefinitionCmdExe createFlowDefinitionCmdExe;
    private final UpdateFlowDefinitionCmdExe updateFlowDefinitionCmdExe;
    private final DeleteFlowDefinitionCmdExe deleteFlowDefinitionCmdExe;
    private final CopyFlowDefinitionCmdExe copyFlowDefinitionCmdExe;
    private final PublishFlowDefinitionCmdExe publishFlowDefinitionCmdExe;
    private final CancelPublishFlowDefinitionCmdExe cancelPublishFlowDefinitionCmdExe;
    private final ActivateFlowDefinitionCmdExe activateFlowDefinitionCmdExe;
    private final SuspendFlowDefinitionCmdExe suspendFlowDefinitionCmdExe;
    private final SyncFlowDefinitionTenantCmdExe syncFlowDefinitionTenantCmdExe;
    private final FindFlowDefinitionByIdQryExe findFlowDefinitionByIdQryExe;
    private final FlowDefinitionPageQryExe flowDefinitionPageQryExe;
    private final ListFlowDefinitionQryExe listFlowDefinitionQryExe;
    private final ListUnpublishedFlowDefinitionQryExe listUnpublishedFlowDefinitionQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this controller does not expose them.")
    public FlowDefinitionController(
            CreateFlowDefinitionCmdExe createFlowDefinitionCmdExe,
            UpdateFlowDefinitionCmdExe updateFlowDefinitionCmdExe,
            DeleteFlowDefinitionCmdExe deleteFlowDefinitionCmdExe,
            CopyFlowDefinitionCmdExe copyFlowDefinitionCmdExe,
            PublishFlowDefinitionCmdExe publishFlowDefinitionCmdExe,
            CancelPublishFlowDefinitionCmdExe cancelPublishFlowDefinitionCmdExe,
            ActivateFlowDefinitionCmdExe activateFlowDefinitionCmdExe,
            SuspendFlowDefinitionCmdExe suspendFlowDefinitionCmdExe,
            SyncFlowDefinitionTenantCmdExe syncFlowDefinitionTenantCmdExe,
            FindFlowDefinitionByIdQryExe findFlowDefinitionByIdQryExe,
            FlowDefinitionPageQryExe flowDefinitionPageQryExe,
            ListFlowDefinitionQryExe listFlowDefinitionQryExe,
            ListUnpublishedFlowDefinitionQryExe listUnpublishedFlowDefinitionQryExe) {
        this.createFlowDefinitionCmdExe = createFlowDefinitionCmdExe;
        this.updateFlowDefinitionCmdExe = updateFlowDefinitionCmdExe;
        this.deleteFlowDefinitionCmdExe = deleteFlowDefinitionCmdExe;
        this.copyFlowDefinitionCmdExe = copyFlowDefinitionCmdExe;
        this.publishFlowDefinitionCmdExe = publishFlowDefinitionCmdExe;
        this.cancelPublishFlowDefinitionCmdExe = cancelPublishFlowDefinitionCmdExe;
        this.activateFlowDefinitionCmdExe = activateFlowDefinitionCmdExe;
        this.suspendFlowDefinitionCmdExe = suspendFlowDefinitionCmdExe;
        this.syncFlowDefinitionTenantCmdExe = syncFlowDefinitionTenantCmdExe;
        this.findFlowDefinitionByIdQryExe = findFlowDefinitionByIdQryExe;
        this.flowDefinitionPageQryExe = flowDefinitionPageQryExe;
        this.listFlowDefinitionQryExe = listFlowDefinitionQryExe;
        this.listUnpublishedFlowDefinitionQryExe = listUnpublishedFlowDefinitionQryExe;
    }

    @GetMapping
    public R<PageResult<FlowDefinitionDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<FlowDefinition> pageResult =
                flowDefinitionPageQryExe.execute(new FlowDefinitionPageQry(new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(this::toDTO).toList(),
                pageResult.totalPages()));
    }

    @GetMapping("/{id}")
    public R<FlowDefinitionDTO> getById(@PathVariable Long id) {
        FlowDefinition definition = findDefinition(id);
        return R.ok(toDTO(definition));
    }

    @PostMapping
    public R<FlowDefinitionDTO> create(@Valid @RequestBody FlowDefinitionRequest request) {
        return R.ok(toDTO(createFlowDefinitionCmdExe.execute(new CreateFlowDefinitionCmd(toPayload(request)))));
    }

    @PutMapping("/{id}")
    public R<FlowDefinitionDTO> update(@PathVariable Long id, @Valid @RequestBody FlowDefinitionRequest request) {
        return R.ok(toDTO(updateFlowDefinitionCmdExe.execute(new UpdateFlowDefinitionCmd(id, toPayload(request)))));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        deleteFlowDefinitionCmdExe.execute(new DeleteFlowDefinitionCmd(id));
        return R.ok(true);
    }

    @PostMapping("/{id}/copy")
    public R<FlowDefinitionDTO> copy(@PathVariable Long id, @Valid @RequestBody FlowDefinitionCopyRequest request) {
        return R.ok(toDTO(copyFlowDefinitionCmdExe.execute(
                new CopyFlowDefinitionCmd(id, request.flowCode(), request.flowName(), null))));
    }

    @PostMapping("/{id}/sync-tenant")
    public R<FlowDefinitionDTO> syncTenant(
            @PathVariable Long id, @Valid @RequestBody FlowDefinitionTenantSyncRequest request) {
        return R.ok(
                toDTO(syncFlowDefinitionTenantCmdExe.execute(new SyncFlowDefinitionTenantCmd(id, request.tenantId()))));
    }

    @PostMapping("/{id}/publish")
    public R<FlowDefinitionDTO> publish(@PathVariable Long id) {
        return R.ok(toDTO(publishFlowDefinitionCmdExe.execute(new ChangeFlowDefinitionStateCmd(id))));
    }

    @PostMapping("/{id}/cancel-publish")
    public R<FlowDefinitionDTO> cancelPublish(@PathVariable Long id) {
        return R.ok(toDTO(cancelPublishFlowDefinitionCmdExe.execute(new ChangeFlowDefinitionStateCmd(id))));
    }

    @PostMapping("/{id}/activate")
    public R<FlowDefinitionDTO> activate(@PathVariable Long id) {
        return R.ok(toDTO(activateFlowDefinitionCmdExe.execute(new ChangeFlowDefinitionStateCmd(id))));
    }

    @PostMapping("/{id}/suspend")
    public R<FlowDefinitionDTO> suspend(@PathVariable Long id) {
        return R.ok(toDTO(suspendFlowDefinitionCmdExe.execute(new ChangeFlowDefinitionStateCmd(id))));
    }

    @GetMapping("/unpublished")
    public R<List<FlowDefinitionDTO>> unpublished() {
        return R.ok(listUnpublishedFlowDefinitionQryExe.execute(new ListUnpublishedFlowDefinitionQry()).stream()
                .map(this::toDTO)
                .toList());
    }

    @GetMapping("/export")
    public R<List<FlowDefinitionDTO>> export() {
        return R.ok(listFlowDefinitionQryExe.execute(new ListFlowDefinitionQry()).stream()
                .map(this::toDTO)
                .toList());
    }

    @GetMapping("/{id}/json")
    public R<String> definitionJson(@PathVariable Long id) {
        return R.ok(findDefinition(id).getDefinitionJson());
    }

    @GetMapping("/{id}/xml")
    public R<String> definitionXml(@PathVariable Long id) {
        return R.ok(findDefinition(id).getDefinitionXml());
    }

    private FlowDefinition findDefinition(Long id) {
        return findFlowDefinitionByIdQryExe
                .execute(new FindFlowDefinitionByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Flow definition not found: " + id));
    }

    private FlowDefinitionPayload toPayload(FlowDefinitionRequest request) {
        if (request == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow definition request must not be null");
        }
        return new FlowDefinitionPayload(
                request.flowCode(),
                request.flowName(),
                request.modelType(),
                request.categoryId(),
                request.version(),
                request.customForm(),
                request.formPath(),
                request.listener(),
                request.extJson(),
                request.tenantId(),
                request.definitionJson(),
                request.definitionXml());
    }

    private FlowDefinitionDTO toDTO(FlowDefinition definition) {
        return new FlowDefinitionDTO(
                definition.getId(),
                definition.getFlowCode(),
                definition.getFlowName(),
                definition.getModelType(),
                definition.getCategoryId(),
                definition.getVersion(),
                definition.getPublishStatus(),
                definition.getCustomForm(),
                definition.getFormPath(),
                definition.getActiveStatus(),
                definition.getListener(),
                definition.getExtJson(),
                definition.getTenantId(),
                definition.getDefinitionJson(),
                definition.getDefinitionXml());
    }
}
