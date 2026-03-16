package com.aotemiao.artemis.system.app.command;

import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteLookupTypeCmdExeTest {

    @Mock
    private LookupTypeGateway lookupTypeGateway;

    @InjectMocks
    private DeleteLookupTypeCmdExe deleteLookupTypeCmdExe;

    @Test
    void execute_callsGatewayDeleteById_whenExists() {
        Long id = 5L;
        DeleteLookupTypeCmd cmd = new DeleteLookupTypeCmd(id);
        LookupType existing = new LookupType();
        existing.setId(id);
        when(lookupTypeGateway.findById(eq(id))).thenReturn(Optional.of(existing));

        deleteLookupTypeCmdExe.execute(cmd);

        verify(lookupTypeGateway).findById(id);
        verify(lookupTypeGateway).deleteById(id);
    }

    @Test
    void execute_throwsNotFound_whenNotExists() {
        Long id = 999L;
        when(lookupTypeGateway.findById(eq(id))).thenReturn(Optional.empty());

        assertThrows(com.aotemiao.artemis.framework.core.exception.BizException.class,
                () -> deleteLookupTypeCmdExe.execute(new DeleteLookupTypeCmd(id)));

        verify(lookupTypeGateway).findById(id);
        verify(lookupTypeGateway, never()).deleteById(any());
    }
}
