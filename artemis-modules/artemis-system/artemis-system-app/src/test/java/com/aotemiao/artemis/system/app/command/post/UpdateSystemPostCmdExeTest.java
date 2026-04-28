package com.aotemiao.artemis.system.app.command.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
class UpdateSystemPostCmdExeTest {

    @Mock
    private SystemPostGateway systemPostGateway;

    @Mock
    private SystemDepartmentGateway systemDepartmentGateway;

    @InjectMocks
    private UpdateSystemPostCmdExe updateSystemPostCmdExe;

    @Test
    void execute_whenUnique_updatesPost() {
        when(systemPostGateway.findById(1L)).thenReturn(Optional.of(samplePost(1L, 1L, "dev", "开发工程师")));
        when(systemDepartmentGateway.findById(2L)).thenReturn(Optional.of(sampleDepartment()));
        when(systemPostGateway.findByPostCode("rd")).thenReturn(Optional.empty());
        when(systemPostGateway.findByDeptIdAndPostName(2L, "研发工程师")).thenReturn(Optional.empty());
        when(systemPostGateway.save(any(SystemPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SystemPost result = updateSystemPostCmdExe.execute(
                new UpdateSystemPostCmd(1L, 2L, "rd", "TECH", "研发工程师", 20, "NORMAL", null));

        assertThat(result.getDeptId()).isEqualTo(2L);
        assertThat(result.getPostCode()).isEqualTo("rd");
    }

    @Test
    void execute_whenNameDuplicatedUnderDepartment_throwsBizException() {
        when(systemPostGateway.findById(1L)).thenReturn(Optional.of(samplePost(1L, 1L, "dev", "开发工程师")));
        when(systemDepartmentGateway.findById(1L)).thenReturn(Optional.of(sampleDepartment()));
        when(systemPostGateway.findByPostCode("dev")).thenReturn(Optional.of(samplePost(1L, 1L, "dev", "开发工程师")));
        when(systemPostGateway.findByDeptIdAndPostName(1L, "开发工程师"))
                .thenReturn(Optional.of(samplePost(2L, 1L, "qa", "开发工程师")));

        assertThatThrownBy(() -> updateSystemPostCmdExe.execute(
                        new UpdateSystemPostCmd(1L, 1L, "dev", "TECH", "开发工程师", 10, "NORMAL", null)))
                .isInstanceOf(BizException.class);
    }

    private static SystemDepartment sampleDepartment() {
        SystemDepartment department = new SystemDepartment();
        department.setId(1L);
        department.setStatus("NORMAL");
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
