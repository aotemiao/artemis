package com.aotemiao.artemis.system.app.command.notice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class DeleteSystemNoticeCmdExeTest {

    @Mock
    private SystemNoticeGateway systemNoticeGateway;

    @InjectMocks
    private DeleteSystemNoticeCmdExe deleteSystemNoticeCmdExe;

    @Test
    void execute_whenNoticeExists_deletesNotice() {
        when(systemNoticeGateway.findById(1L)).thenReturn(Optional.of(new SystemNotice()));

        deleteSystemNoticeCmdExe.execute(new DeleteSystemNoticeCmd(1L));

        verify(systemNoticeGateway).deleteById(1L);
    }

    @Test
    void execute_whenNoticeMissing_throwsBizException() {
        when(systemNoticeGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteSystemNoticeCmdExe.execute(new DeleteSystemNoticeCmd(99L)))
                .isInstanceOf(BizException.class);
    }
}
