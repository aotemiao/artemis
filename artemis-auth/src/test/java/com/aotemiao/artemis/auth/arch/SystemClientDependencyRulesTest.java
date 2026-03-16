package com.aotemiao.artemis.auth.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

/**
 * artemis-auth 依赖系统领域模块的架构规则。
 */
class SystemClientDependencyRulesTest {

    private static final String AUTH_PACKAGE = "com.aotemiao.artemis.auth..";

    private final JavaClasses importedClasses =
            new ClassFileImporter().importPackages("com.aotemiao.artemis.auth");

    @Test
    void auth_should_not_depend_on_system_internal_layers() {
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage(AUTH_PACKAGE)
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "com.aotemiao.artemis.system.app..",
                        "com.aotemiao.artemis.system.domain..",
                        "com.aotemiao.artemis.system.infra.."
                )
                .check(importedClasses);
    }
}

