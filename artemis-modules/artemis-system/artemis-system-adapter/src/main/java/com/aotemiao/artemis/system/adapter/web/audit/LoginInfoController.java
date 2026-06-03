package com.aotemiao.artemis.system.adapter.web.audit;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.framework.core.domain.R;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.adapter.web.dto.audit.DeleteAuditLogRequest;
import com.aotemiao.artemis.system.adapter.web.dto.audit.LoginInfoDTO;
import com.aotemiao.artemis.system.app.command.audit.ClearLoginInfoCmd;
import com.aotemiao.artemis.system.app.command.audit.ClearLoginInfoCmdExe;
import com.aotemiao.artemis.system.app.command.audit.DeleteLoginInfoCmd;
import com.aotemiao.artemis.system.app.command.audit.DeleteLoginInfoCmdExe;
import com.aotemiao.artemis.system.app.query.audit.FindLoginInfoByIdQry;
import com.aotemiao.artemis.system.app.query.audit.FindLoginInfoByIdQryExe;
import com.aotemiao.artemis.system.app.query.audit.LoginInfoPageQry;
import com.aotemiao.artemis.system.app.query.audit.LoginInfoPageQryExe;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 登录访问日志 REST API。 */
@RestController
@RequestMapping(LoginInfoController.BASE_PATH)
public class LoginInfoController {

    public static final String BASE_PATH = "/api/login-infos";

    private final LoginInfoPageQryExe loginInfoPageQryExe;
    private final FindLoginInfoByIdQryExe findLoginInfoByIdQryExe;
    private final DeleteLoginInfoCmdExe deleteLoginInfoCmdExe;
    private final ClearLoginInfoCmdExe clearLoginInfoCmdExe;

    public LoginInfoController(
            LoginInfoPageQryExe loginInfoPageQryExe,
            FindLoginInfoByIdQryExe findLoginInfoByIdQryExe,
            DeleteLoginInfoCmdExe deleteLoginInfoCmdExe,
            ClearLoginInfoCmdExe clearLoginInfoCmdExe) {
        this.loginInfoPageQryExe = loginInfoPageQryExe;
        this.findLoginInfoByIdQryExe = findLoginInfoByIdQryExe;
        this.deleteLoginInfoCmdExe = deleteLoginInfoCmdExe;
        this.clearLoginInfoCmdExe = clearLoginInfoCmdExe;
    }

    @GetMapping
    public R<PageResult<LoginInfoDTO>> page(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        PageResult<LoginInfo> pr = loginInfoPageQryExe.execute(new LoginInfoPageQry(new PageRequest(page, size)));
        return R.ok(
                PageResult.of(pr.total(), pr.content().stream().map(this::toDTO).toList(), pr.totalPages()));
    }

    @GetMapping("/{id}")
    public R<LoginInfoDTO> getById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Invalid id: " + id);
        }
        LoginInfo loginInfo = findLoginInfoByIdQryExe
                .execute(new FindLoginInfoByIdQry(id))
                .orElseThrow(() -> new BizException(CommonErrorCode.NOT_FOUND, "LoginInfo not found: " + id));
        return R.ok(toDTO(loginInfo));
    }

    @OperLogRecord(title = "登录日志", businessType = "DELETE")
    @DeleteMapping
    public R<Void> delete(@Valid @RequestBody DeleteAuditLogRequest request) {
        deleteLoginInfoCmdExe.execute(new DeleteLoginInfoCmd(request.ids()));
        return R.ok();
    }

    @OperLogRecord(title = "登录日志", businessType = "CLEAN")
    @PostMapping("/clear")
    public R<Void> clear() {
        clearLoginInfoCmdExe.execute(new ClearLoginInfoCmd());
        return R.ok();
    }

    private LoginInfoDTO toDTO(LoginInfo loginInfo) {
        return new LoginInfoDTO(
                loginInfo.getId(),
                loginInfo.getTenantId(),
                loginInfo.getUsername(),
                loginInfo.getClientId(),
                loginInfo.getDeviceType(),
                loginInfo.getIpaddr(),
                loginInfo.getLoginLocation(),
                loginInfo.getBrowser(),
                loginInfo.getOs(),
                loginInfo.getStatus(),
                loginInfo.getMsg(),
                loginInfo.getLoginTime());
    }
}
