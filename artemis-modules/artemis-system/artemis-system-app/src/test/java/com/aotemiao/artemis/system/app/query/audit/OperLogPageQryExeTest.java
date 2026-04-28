package com.aotemiao.artemis.system.app.query.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperLogPageQryExeTest {

    @Mock
    private OperLogGateway operLogGateway;

    @InjectMocks
    private OperLogPageQryExe operLogPageQryExe;

    @Test
    void execute_returnsGatewayPage() {
        PageRequest pageRequest = new PageRequest(0, 10);
        OperLog operLog = new OperLog();
        operLog.setTitle("用户管理");
        when(operLogGateway.findPage(pageRequest)).thenReturn(PageResult.of(1, List.of(operLog), 1));

        PageResult<OperLog> result = operLogPageQryExe.execute(new OperLogPageQry(pageRequest));

        assertThat(result.content()).containsExactly(operLog);
        verify(operLogGateway).findPage(pageRequest);
    }
}
