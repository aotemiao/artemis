package com.aotemiao.artemis.resource.infra.gateway.file;

import static org.assertj.core.api.Assertions.assertThat;

import com.aotemiao.artemis.framework.core.domain.PageRequest;
import com.aotemiao.artemis.resource.domain.gateway.file.OssFileGateway;
import com.aotemiao.artemis.resource.domain.model.file.OssFile;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJdbcTest
@Import({
    com.aotemiao.artemis.framework.jdbc.config.JdbcAutoConfiguration.class,
    com.aotemiao.artemis.resource.infra.gateway.file.OssFileGatewayImpl.class
})
@TestPropertySource(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:resource_oss_file_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            "spring.datasource.driver-class-name=org.h2.Driver",
            "spring.sql.init.mode=always",
            "spring.sql.init.schema-locations=classpath:schema.sql",
            "artemis.jdbc.repositories.base-packages=com.aotemiao.artemis.resource.infra.repository"
        })
class OssFileGatewayImplIntegrationTest {

    @Autowired
    private OssFileGateway ossFileGateway;

    @Test
    void save_thenFindAndPage_returnsFileRecord() {
        OssFile saved = ossFileGateway.save(sampleFile("a.png"));

        assertThat(saved.getId()).isNotNull();
        assertThat(ossFileGateway.findById(saved.getId())).isPresent();
        assertThat(ossFileGateway.findByIds(List.of(saved.getId()))).hasSize(1);
        assertThat(ossFileGateway.findPage(new PageRequest(0, 10)).content())
                .extracting(OssFile::getFileName)
                .contains("a.png");
    }

    @Test
    void deleteById_softDeletesFileRecord() {
        OssFile saved = ossFileGateway.save(sampleFile("delete.txt"));

        ossFileGateway.deleteById(saved.getId());

        assertThat(ossFileGateway.findById(saved.getId())).isEmpty();
    }

    private static OssFile sampleFile(String fileName) {
        OssFile ossFile = new OssFile();
        ossFile.setFileName(fileName);
        ossFile.setOriginalFileName(fileName);
        ossFile.setSuffix("txt");
        ossFile.setUrl("/files/" + fileName);
        ossFile.setUploader("admin");
        ossFile.setProvider("LOCAL");
        ossFile.setObjectKey("2026/" + fileName);
        ossFile.setSizeBytes(1L);
        ossFile.setExtJson("{}");
        return ossFile;
    }
}
