package com.aotemiao.artemis.system.app.command.post;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteSystemPostCmdExeTest {

    @Mock
    private SystemPostGateway systemPostGateway;

    @InjectMocks
    private DeleteSystemPostCmdExe deleteSystemPostCmdExe;

    @Test
    void execute_whenNoAssignedUsers_deletesPost() {
        when(systemPostGateway.findById(1L)).thenReturn(Optional.of(samplePost()));
        when(systemPostGateway.countUsersByPostId(1L)).thenReturn(0L);

        deleteSystemPostCmdExe.execute(new DeleteSystemPostCmd(1L));

        verify(systemPostGateway).deleteById(1L);
    }

    @Test
    void execute_whenAssignedToUser_throwsBizException() {
        when(systemPostGateway.findById(1L)).thenReturn(Optional.of(samplePost()));
        when(systemPostGateway.countUsersByPostId(1L)).thenReturn(1L);

        assertThatThrownBy(() -> deleteSystemPostCmdExe.execute(new DeleteSystemPostCmd(1L)))
                .isInstanceOf(BizException.class);
    }

    private static SystemPost samplePost() {
        SystemPost post = new SystemPost();
        post.setId(1L);
        post.setPostCode("dev");
        post.setPostName("开发工程师");
        post.setStatus("NORMAL");
        return post;
    }
}
