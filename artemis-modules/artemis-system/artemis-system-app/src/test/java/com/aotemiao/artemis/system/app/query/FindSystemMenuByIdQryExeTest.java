package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindSystemMenuByIdQryExeTest {

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @InjectMocks
    private FindSystemMenuByIdQryExe findSystemMenuByIdQryExe;

    @Test
    void execute_returnsGatewayResult() {
        SystemMenu menu = new SystemMenu();
        menu.setId(1L);
        when(systemMenuGateway.findById(1L)).thenReturn(Optional.of(menu));

        Optional<SystemMenu> result = findSystemMenuByIdQryExe.execute(new FindSystemMenuByIdQry(1L));

        assertThat(result).contains(menu);
        verify(systemMenuGateway).findById(1L);
    }
}
