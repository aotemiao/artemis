package com.aotemiao.artemis.system.infra.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.system.domain.gateway.post.SystemPostGateway;
import com.aotemiao.artemis.system.domain.model.post.SystemPost;
import com.aotemiao.artemis.system.infra.dataobject.post.SystemUserPostDO;
import com.aotemiao.artemis.system.infra.repository.post.SystemUserPostRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.post.SystemPostGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:system_post_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.system.infra.repository"
        })
class SystemPostGatewayImplIntegrationTest {

    @Autowired
    private SystemPostGateway systemPostGateway;

    @Autowired
    private SystemUserPostRepository systemUserPostRepository;

    @Test
    void save_thenFindByIdAndCode_returnsSameData() {
        SystemPost saved = systemPostGateway.save(samplePost(1L, "dev", "开发工程师"));

        Optional<SystemPost> foundById = systemPostGateway.findById(saved.getId());
        Optional<SystemPost> foundByCode = systemPostGateway.findByPostCode("dev");

        assertThat(foundById).isPresent();
        assertThat(foundById.get().getPostName()).isEqualTo("开发工程师");
        assertThat(foundByCode).isPresent();
    }

    @Test
    void findPage_returnsPostsInSortOrder() {
        systemPostGateway.save(samplePost(1L, "qa", "测试工程师"));
        systemPostGateway.save(samplePost(1L, "dev", "开发工程师"));

        assertThat(systemPostGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(SystemPost::getPostCode)
                .contains("qa", "dev");
    }

    @Test
    void countUsersByPostId_returnsAssignedCount() {
        SystemPost saved = systemPostGateway.save(samplePost(1L, "dev", "开发工程师"));
        SystemUserPostDO relation = new SystemUserPostDO();
        relation.setUserId(1L);
        relation.setPostId(saved.getId());
        systemUserPostRepository.save(relation);

        assertThat(systemPostGateway.countUsersByPostId(saved.getId())).isEqualTo(1);
    }

    @Test
    void deleteById_hidesPostFromQueries() {
        SystemPost saved = systemPostGateway.save(samplePost(1L, "dev", "开发工程师"));

        systemPostGateway.deleteById(saved.getId());

        assertThat(systemPostGateway.findById(saved.getId())).isEmpty();
        assertThat(systemPostGateway.findByPostCode("dev")).isEmpty();
    }

    private static SystemPost samplePost(Long deptId, String postCode, String postName) {
        SystemPost post = new SystemPost();
        post.setDeptId(deptId);
        post.setPostCode(postCode);
        post.setPostCategory("TECH");
        post.setPostName(postName);
        post.setSortOrder(10);
        post.setStatus("NORMAL");
        return post;
    }
}
