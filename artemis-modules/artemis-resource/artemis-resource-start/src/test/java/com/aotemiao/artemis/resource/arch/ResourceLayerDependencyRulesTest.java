package com.aotemiao.artemis.resource.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

/** artemis-resource 分层依赖规则。 */
class ResourceLayerDependencyRulesTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.aotemiao.artemis.resource");

    @Test
    void adapter_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.resource.adapter..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.resource.infra..")
                .check(importedClasses);
    }

    @Test
    void app_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.resource.app..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.resource.infra..")
                .check(importedClasses);
    }

    @Test
    void domain_should_not_depend_on_other_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.resource.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aotemiao.artemis.resource.adapter..",
                        "com.aotemiao.artemis.resource.app..",
                        "com.aotemiao.artemis.resource.infra..")
                .check(importedClasses);
    }

    @Test
    void infra_should_not_depend_on_adapter_or_app() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.resource.infra..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.resource.adapter..", "com.aotemiao.artemis.resource.app..")
                .check(importedClasses);
    }

    @Test
    void client_should_not_depend_on_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.resource.client..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aotemiao.artemis.resource.adapter..",
                        "com.aotemiao.artemis.resource.app..",
                        "com.aotemiao.artemis.resource.domain..",
                        "com.aotemiao.artemis.resource.infra..",
                        "com.aotemiao.artemis.resource.start..")
                .check(importedClasses);
    }
}
