package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LookupTypePageQryExeTest {

    @Mock
    private LookupTypeGateway lookupTypeGateway;

    @InjectMocks
    private LookupTypePageQryExe lookupTypePageQryExe;

    @Test
    void execute_delegatesToGatewayFindPage_andReturnsResult() {
        PageRequest pageRequest = new PageRequest(0, 10);
        LookupTypePageQry qry = new LookupTypePageQry(pageRequest);
        LookupType type = new LookupType();
        type.setId(1L);
        type.setCode("GENDER");
        PageResult<LookupType> expected = PageResult.of(1, List.of(type), 10);
        when(lookupTypeGateway.findPage(pageRequest)).thenReturn(expected);

        PageResult<LookupType> result = lookupTypePageQryExe.execute(qry);

        verify(lookupTypeGateway).findPage(pageRequest);
        assertThat(result).isSameAs(expected);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getCode()).isEqualTo("GENDER");
    }
}
