package com.aotemiao.artemis.system.app.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetLookupItemsByTypeCodeQryExeTest {

    @Mock
    private LookupTypeGateway lookupTypeGateway;

    @InjectMocks
    private GetLookupItemsByTypeCodeQryExe getLookupItemsByTypeCodeQryExe;

    @Test
    void execute_callsFindItemsByTypeCode_withQryTypeCode_andReturnsGatewayResult() {
        String typeCode = "GENDER";
        GetLookupItemsByTypeCodeQry qry = new GetLookupItemsByTypeCodeQry(typeCode);
        LookupItem item1 = new LookupItem();
        item1.setValue("1");
        item1.setLabel("Male");
        item1.setSortOrder(1);
        LookupItem item2 = new LookupItem();
        item2.setValue("2");
        item2.setLabel("Female");
        item2.setSortOrder(2);
        List<LookupItem> items = List.of(item1, item2);
        when(lookupTypeGateway.findItemsByTypeCode(typeCode)).thenReturn(items);

        List<LookupItem> result = getLookupItemsByTypeCodeQryExe.execute(qry);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(lookupTypeGateway).findItemsByTypeCode(captor.capture());
        assertThat(captor.getValue()).isEqualTo(typeCode);
        assertThat(result).isSameAs(items);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue()).isEqualTo("1");
        assertThat(result.get(1).getLabel()).isEqualTo("Female");
    }
}
