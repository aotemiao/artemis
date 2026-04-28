package com.aotemiao.artemis.system.app.query.menu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.SystemMenuGateway;
import com.aotemiao.artemis.system.domain.model.SystemMenu;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListSystemMenusQryExeTest {

    @Mock
    private SystemMenuGateway systemMenuGateway;

    @InjectMocks
    private ListSystemMenusQryExe listSystemMenusQryExe;

    @Test
    void execute_returnsGatewayResult() {
        SystemMenu menu = new SystemMenu();
        menu.setId(1L);
        when(systemMenuGateway.findAll()).thenReturn(List.of(menu));

        List<SystemMenu> result = listSystemMenusQryExe.execute(new ListSystemMenusQry());

        assertThat(result).containsExactly(menu);
        verify(systemMenuGateway).findAll();
    }
}
