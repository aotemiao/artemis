package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.CreateLookupTypeRequest;
import com.aotemiao.artemis.system.adapter.web.dto.LookupItemDTO;
import com.aotemiao.artemis.system.adapter.web.dto.LookupTypeDTO;
import com.aotemiao.artemis.system.adapter.web.dto.UpdateLookupTypeRequest;
import com.aotemiao.artemis.system.app.command.CreateLookupTypeCmd;
import com.aotemiao.artemis.system.app.command.CreateLookupTypeCmdExe;
import com.aotemiao.artemis.system.app.command.DeleteLookupTypeCmd;
import com.aotemiao.artemis.system.app.command.DeleteLookupTypeCmdExe;
import com.aotemiao.artemis.system.app.command.UpdateLookupTypeCmd;
import com.aotemiao.artemis.system.app.command.UpdateLookupTypeCmdExe;
import com.aotemiao.artemis.system.app.query.FindLookupTypeByIdQry;
import com.aotemiao.artemis.system.app.query.FindLookupTypeByIdQryExe;
import com.aotemiao.artemis.system.app.query.GetLookupItemsByTypeCodeQry;
import com.aotemiao.artemis.system.app.query.GetLookupItemsByTypeCodeQryExe;
import com.aotemiao.artemis.system.app.query.LookupTypePageQry;
import com.aotemiao.artemis.system.app.query.LookupTypePageQryExe;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 字典类型 REST API。
 * <ul>
 *   <li>getById: 非法 id（null 或 ≤0）抛 BAD_REQUEST(400)；不存在抛 NOT_FOUND(404)。</li>
 *   <li>getItemsByTypeCode: typeCode 为空/空白抛 BAD_REQUEST(400)；未知 typeCode 返回 200 空列表。</li>
 * </ul>
 */
@RestController
@RequestMapping(LookupTypeController.BASE_PATH)
public class LookupTypeController {

    public static final String BASE_PATH = "/api/lookup-types";

    private final CreateLookupTypeCmdExe createLookupTypeCmdExe;
    private final UpdateLookupTypeCmdExe updateLookupTypeCmdExe;
    private final DeleteLookupTypeCmdExe deleteLookupTypeCmdExe;
    private final LookupTypePageQryExe lookupTypePageQryExe;
    private final FindLookupTypeByIdQryExe findLookupTypeByIdQryExe;
    private final GetLookupItemsByTypeCodeQryExe getLookupItemsByTypeCodeQryExe;

    public LookupTypeController(CreateLookupTypeCmdExe createLookupTypeCmdExe,
                                UpdateLookupTypeCmdExe updateLookupTypeCmdExe,
                                DeleteLookupTypeCmdExe deleteLookupTypeCmdExe,
                                LookupTypePageQryExe lookupTypePageQryExe,
                                FindLookupTypeByIdQryExe findLookupTypeByIdQryExe,
                                GetLookupItemsByTypeCodeQryExe getLookupItemsByTypeCodeQryExe) {
        this.createLookupTypeCmdExe = createLookupTypeCmdExe;
        this.updateLookupTypeCmdExe = updateLookupTypeCmdExe;
        this.deleteLookupTypeCmdExe = deleteLookupTypeCmdExe;
        this.lookupTypePageQryExe = lookupTypePageQryExe;
        this.findLookupTypeByIdQryExe = findLookupTypeByIdQryExe;
        this.getLookupItemsByTypeCodeQryExe = getLookupItemsByTypeCodeQryExe;
    }

    @PostMapping
    public R<LookupTypeDTO> create(@Valid @RequestBody CreateLookupTypeRequest request) {
        var cmd = new CreateLookupTypeCmd(
                request.code(),
                request.name(),
                request.description(),
                toItemCmds(request.items()));
        LookupType t = createLookupTypeCmdExe.execute(cmd);
        return R.ok(toDTO(t));
    }

    @PutMapping("/{id}")
    public R<LookupTypeDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateLookupTypeRequest request) {
        var cmd = new UpdateLookupTypeCmd(
                id,
                request.code(),
                request.name(),
                request.description(),
                toItemCmds(request.items()));
        LookupType t = updateLookupTypeCmdExe.execute(cmd);
        return R.ok(toDTO(t));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteLookupTypeCmdExe.execute(new DeleteLookupTypeCmd(id));
        return R.ok();
    }

    /**
     * 非法 id 返回 400；不存在返回 404。
     */
    @GetMapping("/{id}")
    public R<LookupTypeDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        LookupType t = findLookupTypeByIdQryExe.execute(new FindLookupTypeByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "LookupType not found: " + id));
        return R.ok(toDTO(t));
    }

    @GetMapping
    public R<PageResult<LookupTypeDTO>> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<LookupType> pr = lookupTypePageQryExe.execute(new LookupTypePageQry(new PageRequest(page, size)));
        PageResult<LookupTypeDTO> dtoPage = PageResult.of(pr.total(),
                pr.content().stream().map(this::toDTO).toList(),
                pr.totalPages());
        return R.ok(dtoPage);
    }

    /**
     * typeCode 为空/空白返回 400；未知 typeCode 返回 200 空列表。
     */
    @GetMapping("/{typeCode}/items")
    public R<List<LookupItemDTO>> getItemsByTypeCode(@PathVariable String typeCode) {
        if (typeCode == null || typeCode.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "typeCode must not be blank");
        }
        List<LookupItem> items = getLookupItemsByTypeCodeQryExe.execute(new GetLookupItemsByTypeCodeQry(typeCode));
        return R.ok(items.stream().map(this::toItemDTO).toList());
    }

    private static List<CreateLookupTypeCmd.LookupItemCmd> toItemCmds(List<CreateLookupTypeRequest.LookupItemRequest> items) {
        if (items == null) {
            return null;
        }
        return items.stream()
                .map(i -> new CreateLookupTypeCmd.LookupItemCmd(i.value(), i.label(), i.sortOrder()))
                .toList();
    }

    private LookupTypeDTO toDTO(LookupType t) {
        List<LookupItemDTO> itemDTOs = t.getItems() != null
                ? t.getItems().stream().map(this::toItemDTO).toList()
                : List.of();
        return new LookupTypeDTO(t.getId(), t.getCode(), t.getName(), t.getDescription(), itemDTOs);
    }

    private LookupItemDTO toItemDTO(LookupItem i) {
        return new LookupItemDTO(i.getId(), i.getValue(), i.getLabel(), i.getSortOrder());
    }
}
