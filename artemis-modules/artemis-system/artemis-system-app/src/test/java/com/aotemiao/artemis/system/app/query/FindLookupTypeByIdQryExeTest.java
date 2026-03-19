package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupType;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindLookupTypeByIdQryExeTest {

    @Mock
    private LookupTypeGateway lookupTypeGateway;

    @InjectMocks
    private FindLookupTypeByIdQryExe findLookupTypeByIdQryExe;

    @Test
    void execute_returnsGatewayFindByIdResult() {
        Long id = 20L;
        FindLookupTypeByIdQry qry = new FindLookupTypeByIdQry(id);
        LookupType type = new LookupType();
        type.setId(id);
        type.setCode("GENDER");
        when(lookupTypeGateway.findById(id)).thenReturn(Optional.of(type));

        Optional<LookupType> result = findLookupTypeByIdQryExe.execute(qry);

        verify(lookupTypeGateway).findById(id);
        assertThat(result).isPresent().containsSame(type);
    }

    @Test
    void execute_whenNotFound_returnsEmpty() {
        Long id = 999L;
        FindLookupTypeByIdQry qry = new FindLookupTypeByIdQry(id);
        when(lookupTypeGateway.findById(id)).thenReturn(Optional.empty());

        Optional<LookupType> result = findLookupTypeByIdQryExe.execute(qry);

        assertThat(result).isEmpty();
    }
}
