package com.aotemiao.artemis.system.adapter.web;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.DeleteAuditLogRequest;
import com.aotemiao.artemis.system.adapter.web.dto.OperLogDTO;
import com.aotemiao.artemis.system.app.command.audit.ClearOperLogCmd;
import com.aotemiao.artemis.system.app.command.audit.ClearOperLogCmdExe;
import com.aotemiao.artemis.system.app.command.audit.DeleteOperLogCmd;
import com.aotemiao.artemis.system.app.command.audit.DeleteOperLogCmdExe;
import com.aotemiao.artemis.system.app.query.audit.FindOperLogByIdQry;
import com.aotemiao.artemis.system.app.query.audit.FindOperLogByIdQryExe;
import com.aotemiao.artemis.system.app.query.audit.OperLogPageQry;
import com.aotemiao.artemis.system.app.query.audit.OperLogPageQryExe;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 后台操作日志 REST API。 */
@RestController
@RequestMapping(OperLogController.BASE_PATH)
public class OperLogController {

    public static final String BASE_PATH = "/api/oper-logs";

    private final OperLogPageQryExe operLogPageQryExe;
    private final FindOperLogByIdQryExe findOperLogByIdQryExe;
    private final DeleteOperLogCmdExe deleteOperLogCmdExe;
    private final ClearOperLogCmdExe clearOperLogCmdExe;

    public OperLogController(
            OperLogPageQryExe operLogPageQryExe,
            FindOperLogByIdQryExe findOperLogByIdQryExe,
            DeleteOperLogCmdExe deleteOperLogCmdExe,
            ClearOperLogCmdExe clearOperLogCmdExe) {
        this.operLogPageQryExe = operLogPageQryExe;
        this.findOperLogByIdQryExe = findOperLogByIdQryExe;
        this.deleteOperLogCmdExe = deleteOperLogCmdExe;
        this.clearOperLogCmdExe = clearOperLogCmdExe;
    }

    @GetMapping
    public R<PageResult<OperLogDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<OperLog> pr = operLogPageQryExe.execute(new OperLogPageQry(new PageRequest(page, size)));
        return R.ok(
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages()));
    }

    @GetMapping("/{id}")
    public R<OperLogDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        OperLog operLog = findOperLogByIdQryExe
                .execute(new FindOperLogByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "OperLog not found: " + id));
        return R.ok(toDTO(operLog));
    }

    @OperLogRecord(title = "操作日志", businessType = "DELETE")
    @DeleteMapping
    public R<Void> delete(@Valid @RequestBody DeleteAuditLogRequest request) {
        deleteOperLogCmdExe.execute(new DeleteOperLogCmd(request.ids()));
        return R.ok();
    }

    @OperLogRecord(title = "操作日志", businessType = "CLEAN")
    @PostMapping("/clear")
    public R<Void> clear() {
        clearOperLogCmdExe.execute(new ClearOperLogCmd());
        return R.ok();
    }

    private OperLogDTO toDTO(OperLog operLog) {
        return new OperLogDTO(
                operLog.getId(),
                operLog.getTitle(),
                operLog.getBusinessType(),
                operLog.getMethod(),
                operLog.getRequestMethod(),
                operLog.getOperatorType(),
                operLog.getOperName(),
                operLog.getDeptName(),
                operLog.getOperUrl(),
                operLog.getOperIp(),
                operLog.getOperLocation(),
                operLog.getOperParam(),
                operLog.getJsonResult(),
                operLog.getStatus(),
                operLog.getErrorMsg(),
                operLog.getCostTime(),
                operLog.getOperTime());
    }
}
