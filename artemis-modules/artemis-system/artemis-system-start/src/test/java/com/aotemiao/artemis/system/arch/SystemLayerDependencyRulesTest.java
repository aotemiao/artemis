package com.aotemiao.artemis.system.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

/** artemis-system 分层依赖规则。 */
class SystemLayerDependencyRulesTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.aotemiao.artemis.system");

    @Test
    void adapter_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.system.adapter..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.system.infra..")
                .check(importedClasses);
    }

    @Test
    void app_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.system.app..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.system.infra..")
                .check(importedClasses);
    }

    @Test
    void domain_should_not_depend_on_other_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.system.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aotemiao.artemis.system.adapter..",
                        "com.aotemiao.artemis.system.app..",
                        "com.aotemiao.artemis.system.infra..")
                .check(importedClasses);
    }

    @Test
    void infra_should_not_depend_on_adapter_or_app() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.system.infra..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.system.adapter..", "com.aotemiao.artemis.system.app..")
                .check(importedClasses);
    }

    @Test
    void client_should_not_depend_on_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.system.client..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aotemiao.artemis.system.adapter..",
                        "com.aotemiao.artemis.system.app..",
                        "com.aotemiao.artemis.system.domain..",
                        "com.aotemiao.artemis.system.infra..",
                        "com.aotemiao.artemis.system.start..")
                .check(importedClasses);
    }
}
