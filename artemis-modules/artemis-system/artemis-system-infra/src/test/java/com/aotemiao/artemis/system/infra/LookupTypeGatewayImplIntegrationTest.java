package com.aotemiao.artemis.system.infra;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.framework.core.domain.PageResult;
import com.aotemiao.artemis.system.domain.gateway.LookupTypeGateway;
import com.aotemiao.artemis.system.domain.model.LookupItem;
import com.aotemiao.artemis.system.domain.model.LookupType;
import com.aotemiao.artemis.system.infra.repository.LookupTypeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@EnableJdbcRepositories(basePackageClasses = LookupTypeRepository.class)
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.system.infra.gateway.LookupTypeGatewayImpl.class
})
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:lookup_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.sql.init.mode=always",
    "spring.sql.init.schema-locations=classpath:schema.sql"
})
class LookupTypeGatewayImplIntegrationTest {

    @Autowired
    private LookupTypeGateway lookupTypeGateway;

    @Test
    void save_thenFindById_returnsSameData() {
        LookupType type = new LookupType();
        type.setCode("GENDER");
        type.setName("Gender");
        type.setDescription("User gender");

        LookupType saved = lookupTypeGateway.save(type);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("GENDER");
        assertThat(saved.getName()).isEqualTo("Gender");

        Optional<LookupType> found = lookupTypeGateway.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getCode()).isEqualTo(saved.getCode());
        assertThat(found.get().getName()).isEqualTo(saved.getName());
        assertThat(found.get().getDescription()).isEqualTo(saved.getDescription());
    }

    @Test
    void findPage_returnsSavedTypes() {
        LookupType a = new LookupType();
        a.setCode("CODE_A");
        a.setName("A");
        lookupTypeGateway.save(a);
        LookupType b = new LookupType();
        b.setCode("CODE_B");
        b.setName("B");
        lookupTypeGateway.save(b);

        PageResult<LookupType> page = lookupTypeGateway.findPage(new PageRequest(0, 10));

        assertThat(page.content()).hasSize(2);
        assertThat(page.total()).isEqualTo(2);
        assertThat(page.content().stream().map(LookupType::getCode)).containsExactlyInAnyOrder("CODE_A", "CODE_B");
    }

    @Test
    void deleteById_thenFindPage_doesNotIncludeDeleted() {
        LookupType type = new LookupType();
        type.setCode("TO_DELETE");
        type.setName("To Delete");
        LookupType saved = lookupTypeGateway.save(type);

        lookupTypeGateway.deleteById(saved.getId());

        PageResult<LookupType> page = lookupTypeGateway.findPage(new PageRequest(0, 10));
        assertThat(page.content().stream().map(LookupType::getCode)).doesNotContain("TO_DELETE");
    }

    @Test
    void findItemsByTypeCode_returnsItemsOrderedBySortOrder() {
        LookupType type = new LookupType();
        type.setCode("STATUS");
        type.setName("Status");
        LookupItem i2 = new LookupItem();
        i2.setValue("B");
        i2.setLabel("Second");
        i2.setSortOrder(20);
        LookupItem i1 = new LookupItem();
        i1.setValue("A");
        i1.setLabel("First");
        i1.setSortOrder(10);
        type.setItems(List.of(i2, i1));
        lookupTypeGateway.save(type);

        List<LookupItem> items = lookupTypeGateway.findItemsByTypeCode("STATUS");

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getSortOrder()).isEqualTo(10);
        assertThat(items.get(0).getValue()).isEqualTo("A");
        assertThat(items.get(1).getSortOrder()).isEqualTo(20);
        assertThat(items.get(1).getValue()).isEqualTo("B");
    }
}
