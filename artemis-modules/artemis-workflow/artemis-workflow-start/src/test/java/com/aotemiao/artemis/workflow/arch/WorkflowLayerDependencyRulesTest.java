package com.aotemiao.artemis.workflow.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

/** artemis-workflow 分层依赖规则。 */
class WorkflowLayerDependencyRulesTest {

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.aotemiao.artemis.workflow");

    @Test
    void adapter_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.workflow.adapter..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.workflow.infra..")
                .check(importedClasses);
    }

    @Test
    void app_should_not_depend_on_infra() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.workflow.app..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.workflow.infra..")
                .check(importedClasses);
    }

    @Test
    void domain_should_not_depend_on_other_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.workflow.domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aotemiao.artemis.workflow.adapter..",
                        "com.aotemiao.artemis.workflow.app..",
                        "com.aotemiao.artemis.workflow.infra..")
                .check(importedClasses);
    }

    @Test
    void infra_should_not_depend_on_adapter_or_app() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.workflow.infra..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.aotemiao.artemis.workflow.adapter..", "com.aotemiao.artemis.workflow.app..")
                .check(importedClasses);
    }

    @Test
    void client_should_not_depend_on_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage("com.aotemiao.artemis.workflow.client..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aotemiao.artemis.workflow.adapter..",
                        "com.aotemiao.artemis.workflow.app..",
                        "com.aotemiao.artemis.workflow.domain..",
                        "com.aotemiao.artemis.workflow.infra..",
                        "com.aotemiao.artemis.workflow.start..")
                .check(importedClasses);
    }
}
