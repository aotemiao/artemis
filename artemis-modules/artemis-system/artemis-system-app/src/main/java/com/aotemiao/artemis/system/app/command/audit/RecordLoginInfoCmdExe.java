package com.aotemiao.artemis.system.app.command.audit;

import com.aotemiao.artemis.system.domain.gateway.audit.LoginInfoGateway;
import com.aotemiao.artemis.system.domain.model.audit.LoginInfo;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class RecordLoginInfoCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final LoginInfoGateway loginInfoGateway;

    public RecordLoginInfoCmdExe(LoginInfoGateway loginInfoGateway) {
        this.loginInfoGateway = loginInfoGateway;
    }

    public LoginInfo execute(RecordLoginInfoCmd cmd) {
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setTenantId(defaultText(cmd.tenantId(), "000000"));
        loginInfo.setUsername(defaultText(cmd.username(), "unknown"));
        loginInfo.setClientId(cmd.clientId());
        loginInfo.setDeviceType(cmd.deviceType());
        loginInfo.setIpaddr(cmd.ipaddr());
        loginInfo.setLoginLocation(defaultText(cmd.loginLocation(), "未知"));
        loginInfo.setBrowser(defaultText(cmd.browser(), "Unknown"));
        loginInfo.setOs(defaultText(cmd.os(), "Unknown"));
        loginInfo.setStatus(defaultText(cmd.status(), "FAIL"));
        loginInfo.setMsg(cmd.msg());
        loginInfo.setLoginTime(LocalDateTime.now());
        return loginInfoGateway.save(loginInfo);
    }

    private static String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
