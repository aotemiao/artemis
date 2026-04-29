package com.aotemiao.artemis.workflow.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.adapter.web.dto.FlowSpelDTO;
import com.aotemiao.artemis.workflow.adapter.web.dto.FlowSpelRequest;
import com.aotemiao.artemis.workflow.app.command.spel.CreateFlowSpelCmd;
import com.aotemiao.artemis.workflow.app.command.spel.CreateFlowSpelCmdExe;
import com.aotemiao.artemis.workflow.app.command.spel.DeleteFlowSpelCmd;
import com.aotemiao.artemis.workflow.app.command.spel.DeleteFlowSpelCmdExe;
import com.aotemiao.artemis.workflow.app.command.spel.FlowSpelPayload;
import com.aotemiao.artemis.workflow.app.command.spel.UpdateFlowSpelCmd;
import com.aotemiao.artemis.workflow.app.command.spel.UpdateFlowSpelCmdExe;
import com.aotemiao.artemis.workflow.app.query.spel.FindFlowSpelByIdQry;
import com.aotemiao.artemis.workflow.app.query.spel.FindFlowSpelByIdQryExe;
import com.aotemiao.artemis.workflow.app.query.spel.FlowSpelPageQry;
import com.aotemiao.artemis.workflow.app.query.spel.FlowSpelPageQryExe;
import com.aotemiao.artemis.workflow.app.query.spel.ListFlowSpelQry;
import com.aotemiao.artemis.workflow.app.query.spel.ListFlowSpelQryExe;
import com.aotemiao.artemis.workflow.domain.model.spel.FlowSpel;
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

/** 流程 SpEL 表达式 REST API。 */
@RestController
@RequestMapping(FlowSpelController.BASE_PATH)
public class FlowSpelController {

    public static final String BASE_PATH = "/api/workflow/spels";

    private final CreateFlowSpelCmdExe createFlowSpelCmdExe;
    private final UpdateFlowSpelCmdExe updateFlowSpelCmdExe;
    private final DeleteFlowSpelCmdExe deleteFlowSpelCmdExe;
    private final FindFlowSpelByIdQryExe findFlowSpelByIdQryExe;
    private final FlowSpelPageQryExe flowSpelPageQryExe;
    private final ListFlowSpelQryExe listFlowSpelQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this controller does not expose them.")
    public FlowSpelController(
            CreateFlowSpelCmdExe createFlowSpelCmdExe,
            UpdateFlowSpelCmdExe updateFlowSpelCmdExe,
            DeleteFlowSpelCmdExe deleteFlowSpelCmdExe,
            FindFlowSpelByIdQryExe findFlowSpelByIdQryExe,
            FlowSpelPageQryExe flowSpelPageQryExe,
            ListFlowSpelQryExe listFlowSpelQryExe) {
        this.createFlowSpelCmdExe = createFlowSpelCmdExe;
        this.updateFlowSpelCmdExe = updateFlowSpelCmdExe;
        this.deleteFlowSpelCmdExe = deleteFlowSpelCmdExe;
        this.findFlowSpelByIdQryExe = findFlowSpelByIdQryExe;
        this.flowSpelPageQryExe = flowSpelPageQryExe;
        this.listFlowSpelQryExe = listFlowSpelQryExe;
    }

    @GetMapping
    public R<PageResult<FlowSpelDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<FlowSpel> pageResult = flowSpelPageQryExe.execute(new FlowSpelPageQry(new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream().map(this::toDTO).toList(),
                pageResult.totalPages()));
    }

    @GetMapping("/{id}")
    public R<FlowSpelDTO> getById(@PathVariable Long id) {
        FlowSpel spel = findFlowSpelByIdQryExe
                .execute(new FindFlowSpelByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Flow SpEL not found: " + id));
        return R.ok(toDTO(spel));
    }

    @PostMapping
    public R<FlowSpelDTO> create(@Valid @RequestBody FlowSpelRequest request) {
        return R.ok(toDTO(createFlowSpelCmdExe.execute(new CreateFlowSpelCmd(toPayload(request)))));
    }

    @PutMapping("/{id}")
    public R<FlowSpelDTO> update(@PathVariable Long id, @Valid @RequestBody FlowSpelRequest request) {
        return R.ok(toDTO(updateFlowSpelCmdExe.execute(new UpdateFlowSpelCmd(id, toPayload(request)))));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        deleteFlowSpelCmdExe.execute(new DeleteFlowSpelCmd(id));
        return R.ok(true);
    }

    @GetMapping("/list")
    public R<List<FlowSpelDTO>> list() {
        return R.ok(listFlowSpelQryExe.execute(new ListFlowSpelQry()).stream()
                .map(this::toDTO)
                .toList());
    }

    @GetMapping("/export")
    public R<List<FlowSpelDTO>> export() {
        return list();
    }

    private FlowSpelPayload toPayload(FlowSpelRequest request) {
        if (request == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow SpEL request must not be null");
        }
        return new FlowSpelPayload(
                request.componentName(),
                request.methodName(),
                request.parameters(),
                request.previewExpression(),
                request.remarks(),
                request.status());
    }

    private FlowSpelDTO toDTO(FlowSpel spel) {
        return new FlowSpelDTO(
                spel.getId(),
                spel.getComponentName(),
                spel.getMethodName(),
                spel.getParameters(),
                spel.getPreviewExpression(),
                spel.getRemarks(),
                spel.getStatus());
    }
}
