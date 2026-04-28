package com.aotemiao.artemis.system.app.command.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSystemNoticeCmdExeTest {

    @Mock
    private SystemNoticeGateway systemNoticeGateway;

    @InjectMocks
    private CreateSystemNoticeCmdExe createSystemNoticeCmdExe;

    @Test
    void execute_createsNotice() {
        SystemNotice saved = sampleNotice();
        when(systemNoticeGateway.save(any(SystemNotice.class))).thenReturn(saved);

        SystemNotice result =
                createSystemNoticeCmdExe.execute(new CreateSystemNoticeCmd("维护通知", "NOTICE", "今晚维护", "NORMAL", "提前通知"));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNoticeTitle()).isEqualTo("维护通知");
        verify(systemNoticeGateway).save(any(SystemNotice.class));
    }

    private static SystemNotice sampleNotice() {
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setId(1L);
        systemNotice.setNoticeTitle("维护通知");
        systemNotice.setNoticeType("NOTICE");
        systemNotice.setNoticeContent("今晚维护");
        systemNotice.setStatus("NORMAL");
        systemNotice.setRemarks("提前通知");
        return systemNotice;
    }
}
