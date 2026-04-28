package com.aotemiao.artemis.system.infra.audit;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.system.domain.gateway.audit.OperLogGateway;
import com.aotemiao.artemis.system.domain.model.audit.OperLog;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.audit.OperLogGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:oper_log_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class OperLogGatewayImplIntegrationTest {

    @Autowired
    private OperLogGateway operLogGateway;

    @Test
    void save_thenFindPageAndDelete_returnsExpectedData() {
        OperLog saved = operLogGateway.save(sampleOperLog("用户管理"));

        assertThat(operLogGateway.findById(saved.getId())).isPresent();
        assertThat(operLogGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(OperLog::getTitle)
                .containsExactly("用户管理");

        operLogGateway.deleteByIds(java.util.List.of(saved.getId()));

        assertThat(operLogGateway.findById(saved.getId())).isEmpty();
    }

    @Test
    void clear_hidesAllRecords() {
        operLogGateway.save(sampleOperLog("用户管理"));
        operLogGateway.save(sampleOperLog("角色管理"));

        operLogGateway.clear();

        assertThat(operLogGateway.findPage(new PageRequest(0, 10)).content()).isEmpty();
    }

    private static OperLog sampleOperLog(String title) {
        OperLog operLog = new OperLog();
        operLog.setTitle(title);
        operLog.setBusinessType("INSERT");
        operLog.setMethod("SystemUserController.create(..)");
        operLog.setRequestMethod("POST");
        operLog.setOperatorType("MANAGE");
        operLog.setOperName("admin");
        operLog.setDeptName("研发部");
        operLog.setOperUrl("/api/users");
        operLog.setOperIp("127.0.0.1");
        operLog.setOperLocation("未知");
        operLog.setOperParam("{}");
        operLog.setJsonResult("{\"code\":0}");
        operLog.setStatus("SUCCESS");
        operLog.setCostTime(12L);
        operLog.setOperTime(LocalDateTime.now());
        return operLog;
    }
}
