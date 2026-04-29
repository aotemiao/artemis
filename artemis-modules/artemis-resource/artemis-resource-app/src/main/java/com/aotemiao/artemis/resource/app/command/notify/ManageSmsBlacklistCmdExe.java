package com.aotemiao.artemis.resource.app.command.notify;

import com.aotemiao.artemis.framework.core.constant.CommonErrorCode;
import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.resource.domain.gateway.notify.SmsBlacklistGateway;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

@Service
public class ManageSmsBlacklistCmdExe {

    @SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "Spring injects gateway as a managed collaborator; this executor does not expose it.")
    private final SmsBlacklistGateway smsBlacklistGateway;

    public ManageSmsBlacklistCmdExe(SmsBlacklistGateway smsBlacklistGateway) {
        this.smsBlacklistGateway = smsBlacklistGateway;
    }

    public void add(ManageSmsBlacklistCmd cmd) {
        smsBlacklistGateway.add(normalize(cmd.phone()));
    }

    public void remove(ManageSmsBlacklistCmd cmd) {
        smsBlacklistGateway.remove(normalize(cmd.phone()));
    }

    private String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "Sms phone must not be blank");
        }
        return phone.strip();
    }
}
