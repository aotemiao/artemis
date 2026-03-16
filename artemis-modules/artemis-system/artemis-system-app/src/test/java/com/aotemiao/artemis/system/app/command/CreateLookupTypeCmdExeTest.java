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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateLookupTypeCmdExeTest {

    @Mock
    private LookupTypeGateway lookupTypeGateway;

    @InjectMocks
    private CreateLookupTypeCmdExe createLookupTypeCmdExe;

    @Test
    void execute_callsGatewaySave_withAggregateFromCommand_andReturnsGatewayResult() {
        CreateLookupTypeCmd cmd = new CreateLookupTypeCmd(
                "user_gender",
                "User Gender",
                "Gender options",
                List.of(new CreateLookupTypeCmd.LookupItemCmd("1", "Male", 1))
        );
        LookupType saved = new LookupType();
        saved.setId(100L);
        saved.setCode(cmd.code());
        saved.setName(cmd.name());
        saved.setDescription(cmd.description());
        when(lookupTypeGateway.save(any(LookupType.class))).thenReturn(saved);

        LookupType result = createLookupTypeCmdExe.execute(cmd);

        ArgumentCaptor<LookupType> captor = ArgumentCaptor.forClass(LookupType.class);
        verify(lookupTypeGateway).save(captor.capture());
        LookupType passed = captor.getValue();
        assertThat(passed.getCode()).isEqualTo(cmd.code());
        assertThat(passed.getName()).isEqualTo(cmd.name());
        assertThat(passed.getDescription()).isEqualTo(cmd.description());
        assertThat(passed.getItems()).hasSize(1);
        assertThat(passed.getItems().get(0).getValue()).isEqualTo("1");
        assertThat(passed.getItems().get(0).getLabel()).isEqualTo("Male");
        assertThat(passed.getItems().get(0).getSortOrder()).isEqualTo(1);
        assertThat(result).isSameAs(saved);
    }
}
