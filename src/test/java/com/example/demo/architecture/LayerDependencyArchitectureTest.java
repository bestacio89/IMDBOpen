package com.example.demo.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LayerDependencyArchitectureTest {
    private static JavaClasses productionClasses;

    @BeforeAll
    static void importProductionClasses() {
        productionClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("exceptions", "com.example.demo", "com.example.imdbopen");
    }

    @Test
    void domainPackagesDoNotDependOnApplicationDeliveryOrInfrastructure() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..application..", "..api..", "..infrastructure..")
                .check(productionClasses);
    }

    @Test
    void applicationPackagesDoNotDependOnSpringDataOrPersistenceAdapters() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework.data..", "..infrastructure.persistence..",
                        "persistence.repository..", "service..", "web..")
                .check(productionClasses);
    }

    @Test
    void applicationPackagesDoNotDependOnHttpApiPackages() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAPackage("..api..")
                .check(productionClasses);
    }

    @Test
    void apiPackagesDoNotDependOnPersistenceImplementations() {
        noClasses().that().resideInAPackage("..api..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure.persistence..", "persistence.repository..", "web..")
                .check(productionClasses);
    }

    @Test
    void contractsDoNotDependOnDeliveryOrPersistenceImplementation() {
        noClasses().that().resideInAPackage("..contracts..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "org.springframework.data..",
                        "..api..", "web..", "persistence..", "..infrastructure..")
                .check(productionClasses);
    }

    @Test
    void ingestionAdaptersDoNotAccessPersistenceDirectly() {
        noClasses().that().resideInAPackage("..ingestion.infrastructure..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework.data..", "org.springframework.jdbc..",
                        "..infrastructure.persistence..", "persistence.repository..")
                .check(productionClasses);
    }

    @Test
    void moduleInfrastructureAdaptersDoNotDependOnHttpApiPackages() {
        noClasses().that().resideInAPackage("..infrastructure.persistence..")
                .should().dependOnClassesThat()
                .resideInAPackage("..api..")
                .check(productionClasses);
    }
}
