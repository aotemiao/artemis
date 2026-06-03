package com.aotemiao.artemis.system.adapter.web.audit;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import com.aotemiao.artemis.system.app.command.audit.RecordOperLogCmdExe;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class OperLogAspectTest {

    @Test
    void annotatedControllerMethod_recordsOperationLog() throws Exception {
        RecordOperLogCmdExe recordOperLogCmdExe = org.mockito.Mockito.mock(RecordOperLogCmdExe.class);
        OperLogAspect aspect = new OperLogAspect(recordOperLogCmdExe, new ObjectMapper());
        AspectJProxyFactory factory = new AspectJProxyFactory(new SampleController());
        factory.addAspect(aspect);
        SampleController proxy = factory.getProxy();

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/sample");
        request.addHeader("X-Username", "admin");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        try {
            proxy.create();
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }

        verify(recordOperLogCmdExe)
                .execute(argThat(cmd -> "测试模块".equals(cmd.title())
                        && "INSERT".equals(cmd.businessType())
                        && "admin".equals(cmd.operName())
                        && "SUCCESS".equals(cmd.status())));
    }

    @RestController
    @RequestMapping("/sample")
    static class SampleController {

        @OperLogRecord(title = "测试模块", businessType = "INSERT")
        @PostMapping
        public String create() {
            return "ok";
        }
    }
}
