package com.aotemiao.artemis.system.app.command.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSystemNoticeCmdExeTest {

    @Mock
    private SystemNoticeGateway systemNoticeGateway;

    @InjectMocks
    private UpdateSystemNoticeCmdExe updateSystemNoticeCmdExe;

    @Test
    void execute_whenNoticeExists_updatesNotice() {
        SystemNotice existing = sampleNotice("维护通知", "NORMAL");
        SystemNotice saved = sampleNotice("维护结束", "CLOSED");
        when(systemNoticeGateway.findById(1L)).thenReturn(Optional.of(existing));
        when(systemNoticeGateway.save(any(SystemNotice.class))).thenReturn(saved);

        SystemNotice result = updateSystemNoticeCmdExe.execute(
                new UpdateSystemNoticeCmd(1L, "维护结束", "NOTICE", "维护已完成", "CLOSED", "完成"));

        assertThat(result.getNoticeTitle()).isEqualTo("维护结束");
        assertThat(result.getStatus()).isEqualTo("CLOSED");
        verify(systemNoticeGateway).save(any(SystemNotice.class));
    }

    @Test
    void execute_whenNoticeMissing_throwsBizException() {
        when(systemNoticeGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateSystemNoticeCmdExe.execute(
                        new UpdateSystemNoticeCmd(99L, "标题", "NOTICE", "内容", "NORMAL", null)))
                .isInstanceOf(BizException.class);
    }

    private static SystemNotice sampleNotice(String noticeTitle, String status) {
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setId(1L);
        systemNotice.setNoticeTitle(noticeTitle);
        systemNotice.setNoticeType("NOTICE");
        systemNotice.setNoticeContent("内容");
        systemNotice.setStatus(status);
        return systemNotice;
    }
}
