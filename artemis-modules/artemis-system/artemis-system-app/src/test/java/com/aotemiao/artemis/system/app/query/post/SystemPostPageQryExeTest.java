package com.aotemiao.artemis.system.app.query.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemPostPageQryExeTest {

    @Mock
    private SystemPostGateway systemPostGateway;

    @InjectMocks
    private SystemPostPageQryExe systemPostPageQryExe;

    @Test
    void execute_returnsGatewayPage() {
        PageRequest pageRequest = new PageRequest(0, 10);
        when(systemPostGateway.findPage(pageRequest)).thenReturn(PageResult.of(1, List.of(samplePost()), 1));

        PageResult<SystemPost> result = systemPostPageQryExe.execute(new SystemPostPageQry(pageRequest));

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.content()).extracting(SystemPost::getPostCode).containsExactly("dev");
    }

    private static SystemPost samplePost() {
        SystemPost post = new SystemPost();
        post.setId(1L);
        post.setPostCode("dev");
        post.setPostName("开发工程师");
        return post;
    }
}
