package com.aotemiao.artemis.workflow.adapter.web.category;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.workflow.adapter.web.dto.category.FlowCategoryDTO;
import com.aotemiao.artemis.workflow.adapter.web.dto.category.FlowCategoryRequest;
import com.aotemiao.artemis.workflow.app.command.category.CreateFlowCategoryCmd;
import com.aotemiao.artemis.workflow.app.command.category.CreateFlowCategoryCmdExe;
import com.aotemiao.artemis.workflow.app.command.category.DeleteFlowCategoryCmd;
import com.aotemiao.artemis.workflow.app.command.category.DeleteFlowCategoryCmdExe;
import com.aotemiao.artemis.workflow.app.command.category.FlowCategoryPayload;
import com.aotemiao.artemis.workflow.app.command.category.UpdateFlowCategoryCmd;
import com.aotemiao.artemis.workflow.app.command.category.UpdateFlowCategoryCmdExe;
import com.aotemiao.artemis.workflow.app.query.category.FindFlowCategoryByIdQry;
import com.aotemiao.artemis.workflow.app.query.category.FindFlowCategoryByIdQryExe;
import com.aotemiao.artemis.workflow.app.query.category.FlowCategoryPageQry;
import com.aotemiao.artemis.workflow.app.query.category.FlowCategoryPageQryExe;
import com.aotemiao.artemis.workflow.app.query.category.ListFlowCategoryQry;
import com.aotemiao.artemis.workflow.app.query.category.ListFlowCategoryQryExe;
import com.aotemiao.artemis.workflow.domain.model.category.FlowCategory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import java.util.ArrayList;
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

/** 流程分类 REST API。 */
@RestController
@RequestMapping(FlowCategoryController.BASE_PATH)
public class FlowCategoryController {

    public static final String BASE_PATH = "/api/workflow/categories";

    private final CreateFlowCategoryCmdExe createFlowCategoryCmdExe;
    private final UpdateFlowCategoryCmdExe updateFlowCategoryCmdExe;
    private final DeleteFlowCategoryCmdExe deleteFlowCategoryCmdExe;
    private final FindFlowCategoryByIdQryExe findFlowCategoryByIdQryExe;
    private final FlowCategoryPageQryExe flowCategoryPageQryExe;
    private final ListFlowCategoryQryExe listFlowCategoryQryExe;

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects executors as managed collaborators; this controller does not expose them.")
    public FlowCategoryController(
            CreateFlowCategoryCmdExe createFlowCategoryCmdExe,
            UpdateFlowCategoryCmdExe updateFlowCategoryCmdExe,
            DeleteFlowCategoryCmdExe deleteFlowCategoryCmdExe,
            FindFlowCategoryByIdQryExe findFlowCategoryByIdQryExe,
            FlowCategoryPageQryExe flowCategoryPageQryExe,
            ListFlowCategoryQryExe listFlowCategoryQryExe) {
        this.createFlowCategoryCmdExe = createFlowCategoryCmdExe;
        this.updateFlowCategoryCmdExe = updateFlowCategoryCmdExe;
        this.deleteFlowCategoryCmdExe = deleteFlowCategoryCmdExe;
        this.findFlowCategoryByIdQryExe = findFlowCategoryByIdQryExe;
        this.flowCategoryPageQryExe = flowCategoryPageQryExe;
        this.listFlowCategoryQryExe = listFlowCategoryQryExe;
    }

    @GetMapping
    public R<PageResult<FlowCategoryDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<FlowCategory> pageResult =
                flowCategoryPageQryExe.execute(new FlowCategoryPageQry(new PageRequest(page, size)));
        return R.ok(PageResult.of(
                pageResult.total(),
                pageResult.content().stream()
                        .map(category -> toDTO(category, List.of()))
                        .toList(),
                pageResult.totalPages()));
    }

    @GetMapping("/{id}")
    public R<FlowCategoryDTO> getById(@PathVariable Long id) {
        FlowCategory category = findFlowCategoryByIdQryExe
                .execute(new FindFlowCategoryByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "Flow category not found: " + id));
        return R.ok(toDTO(category, List.of()));
    }

    @PostMapping
    public R<FlowCategoryDTO> create(@Valid @RequestBody FlowCategoryRequest request) {
        return R.ok(toDTO(createFlowCategoryCmdExe.execute(new CreateFlowCategoryCmd(toPayload(request))), List.of()));
    }

    @PutMapping("/{id}")
    public R<FlowCategoryDTO> update(@PathVariable Long id, @Valid @RequestBody FlowCategoryRequest request) {
        return R.ok(
                toDTO(updateFlowCategoryCmdExe.execute(new UpdateFlowCategoryCmd(id, toPayload(request))), List.of()));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        deleteFlowCategoryCmdExe.execute(new DeleteFlowCategoryCmd(id));
        return R.ok(true);
    }

    @GetMapping("/tree")
    public R<List<FlowCategoryDTO>> tree() {
        return R.ok(toTree(listFlowCategoryQryExe.execute(new ListFlowCategoryQry()), 0L));
    }

    @GetMapping("/export")
    public R<List<FlowCategoryDTO>> export() {
        return R.ok(listFlowCategoryQryExe.execute(new ListFlowCategoryQry()).stream()
                .map(category -> toDTO(category, List.of()))
                .toList());
    }

    private FlowCategoryPayload toPayload(FlowCategoryRequest request) {
        if (request == null) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Flow category request must not be null");
        }
        return new FlowCategoryPayload(
                request.parentId(), request.categoryName(), request.sortOrder(), request.remarks());
    }

    private List<FlowCategoryDTO> toTree(List<FlowCategory> categories, Long parentId) {
        List<FlowCategoryDTO> result = new ArrayList<>();
        for (FlowCategory category : categories) {
            if (parentId.equals(category.getParentId())) {
                result.add(toDTO(category, toTree(categories, category.getId())));
            }
        }
        return result;
    }

    private FlowCategoryDTO toDTO(FlowCategory category, List<FlowCategoryDTO> children) {
        return new FlowCategoryDTO(
                category.getId(),
                category.getParentId(),
                category.getAncestors(),
                category.getCategoryName(),
                category.getSortOrder(),
                category.getRemarks(),
                children);
    }
}
