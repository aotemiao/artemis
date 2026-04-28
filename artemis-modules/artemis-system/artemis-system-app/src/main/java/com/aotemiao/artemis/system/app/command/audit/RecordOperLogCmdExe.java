package com.aotemiao.artemis.system.app.command.audit;

import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class RecordOperLogCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects the gateway as a managed collaborator; this executor does not expose it.")
    private final OperLogGateway operLogGateway;

    public RecordOperLogCmdExe(OperLogGateway operLogGateway) {
        this.operLogGateway = operLogGateway;
    }

    public OperLog execute(RecordOperLogCmd cmd) {
        Long costTime = cmd.costTime();
        OperLog operLog = new OperLog();
        operLog.setTitle(defaultText(cmd.title(), "未命名模块"));
        operLog.setBusinessType(defaultText(cmd.businessType(), "OTHER"));
        operLog.setMethod(cmd.method());
        operLog.setRequestMethod(cmd.requestMethod());
        operLog.setOperatorType(defaultText(cmd.operatorType(), "MANAGE"));
        operLog.setOperName(defaultText(cmd.operName(), "unknown"));
        operLog.setDeptName(cmd.deptName());
        operLog.setOperUrl(cmd.operUrl());
        operLog.setOperIp(cmd.operIp());
        operLog.setOperLocation(defaultText(cmd.operLocation(), "未知"));
        operLog.setOperParam(cmd.operParam());
        operLog.setJsonResult(cmd.jsonResult());
        operLog.setStatus(defaultText(cmd.status(), "SUCCESS"));
        operLog.setErrorMsg(cmd.errorMsg());
        operLog.setCostTime(costTime == null ? Long.valueOf(0L) : costTime);
        operLog.setOperTime(LocalDateTime.now());
        return operLogGateway.save(operLog);
    }

    private static String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
