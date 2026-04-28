package com.aotemiao.artemis.system.app.command.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordOperLogCmdExeTest {

    @Mock
    private OperLogGateway operLogGateway;

    @InjectMocks
    private RecordOperLogCmdExe recordOperLogCmdExe;

    @Test
    void execute_savesOperationLog() {
        OperLog saved = new OperLog();
        saved.setId(1L);
        saved.setTitle("用户管理");
        when(operLogGateway.save(org.mockito.ArgumentMatchers.any())).thenReturn(saved);

        OperLog result = recordOperLogCmdExe.execute(new RecordOperLogCmd(
                "用户管理",
                "INSERT",
                "SystemUserController.create(..)",
                "POST",
                "MANAGE",
                "admin",
                "研发部",
                "/api/users",
                "127.0.0.1",
                "未知",
                "{}",
                "{\"code\":0}",
                "SUCCESS",
                null,
                12L));

        assertThat(result.getId()).isEqualTo(1L);
        verify(operLogGateway)
                .save(argThat(log -> "用户管理".equals(log.getTitle())
                        && "INSERT".equals(log.getBusinessType())
                        && "admin".equals(log.getOperName())
                        && log.getOperTime() != null));
    }
}
