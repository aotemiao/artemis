package com.aotemiao.artemis.system.app.query.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemNoticePageQryExeTest {

    @Mock
    private SystemNoticeGateway systemNoticeGateway;

    @InjectMocks
    private SystemNoticePageQryExe systemNoticePageQryExe;

    @Test
    void execute_returnsGatewayPage() {
        PageRequest pageRequest = new PageRequest(0, 10);
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setId(1L);
        when(systemNoticeGateway.findPage(pageRequest)).thenReturn(PageResult.of(1, List.of(systemNotice), 1));

        PageResult<SystemNotice> result = systemNoticePageQryExe.execute(new SystemNoticePageQry(pageRequest));

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
    }
}
