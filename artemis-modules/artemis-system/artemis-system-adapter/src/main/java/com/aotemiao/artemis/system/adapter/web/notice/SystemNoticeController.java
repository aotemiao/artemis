package com.aotemiao.artemis.system.adapter.web.notice;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.notice.SystemNoticeDTO;
import com.aotemiao.artemis.system.adapter.web.dto.notice.SystemNoticeRequest;
import com.aotemiao.artemis.system.app.command.notice.CreateSystemNoticeCmd;
import com.aotemiao.artemis.system.app.command.notice.CreateSystemNoticeCmdExe;
import com.aotemiao.artemis.system.app.command.notice.DeleteSystemNoticeCmd;
import com.aotemiao.artemis.system.app.command.notice.DeleteSystemNoticeCmdExe;
import com.aotemiao.artemis.system.app.command.notice.UpdateSystemNoticeCmd;
import com.aotemiao.artemis.system.app.command.notice.UpdateSystemNoticeCmdExe;
import com.aotemiao.artemis.system.app.query.notice.FindSystemNoticeByIdQry;
import com.aotemiao.artemis.system.app.query.notice.FindSystemNoticeByIdQryExe;
import com.aotemiao.artemis.system.app.query.notice.SystemNoticePageQry;
import com.aotemiao.artemis.system.app.query.notice.SystemNoticePageQryExe;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
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

/** 系统通知公告 REST API。 */
@RestController
@RequestMapping(SystemNoticeController.BASE_PATH)
public class SystemNoticeController {

    public static final String BASE_PATH = "/api/notices";

    private final CreateSystemNoticeCmdExe createSystemNoticeCmdExe;
    private final UpdateSystemNoticeCmdExe updateSystemNoticeCmdExe;
    private final DeleteSystemNoticeCmdExe deleteSystemNoticeCmdExe;
    private final FindSystemNoticeByIdQryExe findSystemNoticeByIdQryExe;
    private final SystemNoticePageQryExe systemNoticePageQryExe;

    public SystemNoticeController(
            CreateSystemNoticeCmdExe createSystemNoticeCmdExe,
            UpdateSystemNoticeCmdExe updateSystemNoticeCmdExe,
            DeleteSystemNoticeCmdExe deleteSystemNoticeCmdExe,
            FindSystemNoticeByIdQryExe findSystemNoticeByIdQryExe,
            SystemNoticePageQryExe systemNoticePageQryExe) {
        this.createSystemNoticeCmdExe = createSystemNoticeCmdExe;
        this.updateSystemNoticeCmdExe = updateSystemNoticeCmdExe;
        this.deleteSystemNoticeCmdExe = deleteSystemNoticeCmdExe;
        this.findSystemNoticeByIdQryExe = findSystemNoticeByIdQryExe;
        this.systemNoticePageQryExe = systemNoticePageQryExe;
    }

    @PostMapping
    public R<SystemNoticeDTO> create(@Valid @RequestBody SystemNoticeRequest request) {
        SystemNotice systemNotice = createSystemNoticeCmdExe.execute(new CreateSystemNoticeCmd(
                request.noticeTitle(),
                request.noticeType(),
                request.noticeContent(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(systemNotice));
    }

    @PutMapping("/{id}")
    public R<SystemNoticeDTO> update(@PathVariable Long id, @Valid @RequestBody SystemNoticeRequest request) {
        SystemNotice systemNotice = updateSystemNoticeCmdExe.execute(new UpdateSystemNoticeCmd(
                id,
                request.noticeTitle(),
                request.noticeType(),
                request.noticeContent(),
                request.status(),
                request.remarks()));
        return R.ok(toDTO(systemNotice));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deleteSystemNoticeCmdExe.execute(new DeleteSystemNoticeCmd(id));
        return R.ok();
    }

    @GetMapping("/{id}")
    public R<SystemNoticeDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        SystemNotice systemNotice = findSystemNoticeByIdQryExe
                .execute(new FindSystemNoticeByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "SystemNotice not found: " + id));
        return R.ok(toDTO(systemNotice));
    }

    @GetMapping
    public R<PageResult<SystemNoticeDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<SystemNotice> pr =
                systemNoticePageQryExe.execute(new SystemNoticePageQry(new PageRequest(page, size)));
        PageResult<SystemNoticeDTO> dtoPage =
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages());
        return R.ok(dtoPage);
    }

    private SystemNoticeDTO toDTO(SystemNotice systemNotice) {
        return new SystemNoticeDTO(
                systemNotice.getId(),
                systemNotice.getNoticeTitle(),
                systemNotice.getNoticeType(),
                systemNotice.getNoticeContent(),
                systemNotice.getStatus(),
                systemNotice.getRemarks());
    }
}
