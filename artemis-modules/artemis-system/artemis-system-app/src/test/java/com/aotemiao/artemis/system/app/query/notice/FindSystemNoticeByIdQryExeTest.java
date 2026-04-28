package com.aotemiao.artemis.system.app.query.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindSystemNoticeByIdQryExeTest {

    @Mock
    private SystemNoticeGateway systemNoticeGateway;

    @InjectMocks
    private FindSystemNoticeByIdQryExe findSystemNoticeByIdQryExe;

    @Test
    void execute_returnsGatewayResult() {
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setId(1L);
        when(systemNoticeGateway.findById(1L)).thenReturn(Optional.of(systemNotice));

        Optional<SystemNotice> result = findSystemNoticeByIdQryExe.execute(new FindSystemNoticeByIdQry(1L));

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
}
