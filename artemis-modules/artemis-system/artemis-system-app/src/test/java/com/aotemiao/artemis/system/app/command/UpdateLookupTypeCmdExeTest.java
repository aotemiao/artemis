package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLookupTypeCmdExeTest {

    @Mock
    private LookupTypeGateway lookupTypeGateway;

    @InjectMocks
    private UpdateLookupTypeCmdExe updateLookupTypeCmdExe;

    @Test
    void execute_loadsById_thenSavesWithUpdatedFields_andReturnsGatewayResult() {
        Long id = 10L;
        UpdateLookupTypeCmd cmd = new UpdateLookupTypeCmd(
                id,
                "order_status",
                "Order Status",
                "Status options",
                List.of(new CreateLookupTypeCmd.LookupItemCmd("PENDING", "Pending", 0))
        );
        LookupType existing = new LookupType();
        existing.setId(id);
        existing.setCode("old_code");
        existing.setName("Old Name");
        when(lookupTypeGateway.findById(id)).thenReturn(Optional.of(existing));
        LookupType saved = new LookupType();
        saved.setId(id);
        saved.setCode(cmd.code());
        saved.setName(cmd.name());
        when(lookupTypeGateway.save(any(LookupType.class))).thenReturn(saved);

        LookupType result = updateLookupTypeCmdExe.execute(cmd);

        verify(lookupTypeGateway).findById(id);
        ArgumentCaptor<LookupType> captor = ArgumentCaptor.forClass(LookupType.class);
        verify(lookupTypeGateway).save(captor.capture());
        LookupType passed = captor.getValue();
        assertThat(passed.getId()).isEqualTo(id);
        assertThat(passed.getCode()).isEqualTo(cmd.code());
        assertThat(passed.getName()).isEqualTo(cmd.name());
        assertThat(passed.getDescription()).isEqualTo(cmd.description());
        assertThat(passed.getItems()).hasSize(1);
        assertThat(passed.getItems().get(0).getValue()).isEqualTo("PENDING");
        assertThat(result).isSameAs(saved);
    }
}
