package com.aotemiao.artemis.system.infra.notice;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.notice.SystemNoticeGateway;
import com.aotemiao.artemis.system.domain.model.notice.SystemNotice;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.notice.SystemNoticeGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_notice_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class SystemNoticeGatewayImplIntegrationTest {

    @Autowired
    private SystemNoticeGateway systemNoticeGateway;

    @Test
    void save_thenFindById_returnsSameData() {
        SystemNotice saved = systemNoticeGateway.save(sampleNotice("维护通知", "NORMAL"));

        Optional<SystemNotice> found = systemNoticeGateway.findById(saved.getId());

        assertThat(saved.getId()).isNotNull();
        assertThat(found).isPresent();
        assertThat(found.get().getNoticeTitle()).isEqualTo("维护通知");
        assertThat(found.get().getStatus()).isEqualTo("NORMAL");
    }

    @Test
    void findPage_returnsSavedNotices() {
        systemNoticeGateway.save(sampleNotice("维护通知", "NORMAL"));
        systemNoticeGateway.save(sampleNotice("版本发布", "NORMAL"));

        PageResult<SystemNotice> pageResult = systemNoticeGateway.findPage(new PageRequest(0, 10));

        assertThat(pageResult.content()).hasSize(2);
        assertThat(pageResult.content().stream().map(SystemNotice::getNoticeTitle))
                .containsExactlyInAnyOrder("维护通知", "版本发布");
    }

    @Test
    void deleteById_hidesNoticeFromQueries() {
        SystemNotice saved = systemNoticeGateway.save(sampleNotice("维护通知", "NORMAL"));

        systemNoticeGateway.deleteById(saved.getId());

        assertThat(systemNoticeGateway.findById(saved.getId())).isEmpty();
    }

    private static SystemNotice sampleNotice(String noticeTitle, String status) {
        SystemNotice systemNotice = new SystemNotice();
        systemNotice.setNoticeTitle(noticeTitle);
        systemNotice.setNoticeType("NOTICE");
        systemNotice.setNoticeContent("内容");
        systemNotice.setStatus(status);
        systemNotice.setRemarks("remark");
        return systemNotice;
    }
}
