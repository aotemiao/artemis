package com.aotemiao.artemis.system.app.command.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aotemiao.artemis.framework.core.exception.BizException;
import com.aotemiao.artemis.system.domain.gateway.department.SystemDepartmentGateway;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.department.SystemDepartment;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateSystemPostCmdExeTest {

    @Mock
    private SystemPostGateway systemPostGateway;

    @Mock
    private SystemDepartmentGateway systemDepartmentGateway;

    @InjectMocks
    private CreateSystemPostCmdExe createSystemPostCmdExe;

    @Test
    void execute_whenDepartmentIsNormal_createsPost() {
        when(systemDepartmentGateway.findById(1L)).thenReturn(Optional.of(sampleDepartment("NORMAL")));
        when(systemPostGateway.findByPostCode("dev")).thenReturn(Optional.empty());
        when(systemPostGateway.findByDeptIdAndPostName(1L, "开发工程师")).thenReturn(Optional.empty());
        when(systemPostGateway.save(any(SystemPost.class))).thenReturn(samplePost(1L, 1L, "dev", "开发工程师"));

        SystemPost result =
                createSystemPostCmdExe.execute(new CreateSystemPostCmd(1L, "dev", "TECH", "开发工程师", 10, "NORMAL", null));

        assertThat(result.getPostCode()).isEqualTo("dev");
        verify(systemPostGateway).save(any(SystemPost.class));
    }

    @Test
    void execute_whenPostCodeDuplicated_throwsBizException() {
        when(systemDepartmentGateway.findById(1L)).thenReturn(Optional.of(sampleDepartment("NORMAL")));
        when(systemPostGateway.findByPostCode("dev")).thenReturn(Optional.of(samplePost(1L, 1L, "dev", "开发工程师")));

        assertThatThrownBy(() -> createSystemPostCmdExe.execute(
                        new CreateSystemPostCmd(1L, "dev", "TECH", "开发工程师", 10, "NORMAL", null)))
                .isInstanceOf(BizException.class);
    }

    @Test
    void execute_whenDepartmentDisabled_throwsBizException() {
        when(systemDepartmentGateway.findById(1L)).thenReturn(Optional.of(sampleDepartment("DISABLED")));

        assertThatThrownBy(() -> createSystemPostCmdExe.execute(
                        new CreateSystemPostCmd(1L, "dev", "TECH", "开发工程师", 10, "NORMAL", null)))
                .isInstanceOf(BizException.class);
    }

    private static SystemDepartment sampleDepartment(String status) {
        SystemDepartment department = new SystemDepartment();
        department.setId(1L);
        department.setStatus(status);
        return department;
    }

    private static SystemPost samplePost(Long id, Long deptId, String postCode, String postName) {
        SystemPost post = new SystemPost();
        post.setId(id);
        post.setDeptId(deptId);
        post.setPostCode(postCode);
        post.setPostName(postName);
        post.setStatus("NORMAL");
        return post;
    }
}
