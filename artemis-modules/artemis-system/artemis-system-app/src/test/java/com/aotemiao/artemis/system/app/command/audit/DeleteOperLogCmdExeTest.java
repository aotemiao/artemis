package com.aotemiao.artemis.system.app.command.audit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteOperLogCmdExeTest {

    @Mock
    private OperLogGateway operLogGateway;

    @InjectMocks
    private DeleteOperLogCmdExe deleteOperLogCmdExe;

    @Test
    void execute_deletesByIds() {
        deleteOperLogCmdExe.execute(new DeleteOperLogCmd(List.of(1L, 2L)));

        verify(operLogGateway).deleteByIds(List.of(1L, 2L));
    }

    @Test
    void execute_rejectsEmptyIds() {
        assertThatThrownBy(() -> deleteOperLogCmdExe.execute(new DeleteOperLogCmd(List.of())))
                .isInstanceOf(BizException.class);
    }
}
