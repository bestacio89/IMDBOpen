package com.example.demo.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LayerDependencyArchitectureTest {
    private static JavaClasses applicationClasses;

    @BeforeAll
    static void importApplicationClasses() {
        applicationClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("entities", "service", "persistence", "web",
                        "exceptions", "utilities", "com.example.demo", "com.example.imdbopen");
    }

    @Test
    void domainEntitiesDoNotDependOnApplicationOrDeliveryLayers() {
        noClasses().that().resideInAPackage("..entities..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("service..", "web..", "persistence..", "..application..", "..api..")
                .check(applicationClasses);
    }

    @Test
    void modularDomainPackagesDoNotDependOnApplicationOrDeliveryLayers() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("service..", "..application..", "..api..", "web..", "persistence..")
                .check(applicationClasses);
    }

    @Test
    void applicationServicesDoNotDependOnSpringDataOrPersistenceAdapters() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework.data..",
                        "..infrastructure.persistence..", "persistence.repository..")
                .check(applicationClasses);
    }

    @Test
    void moduleApiControllersDoNotDependOnPersistenceRepositories() {
        noClasses().that().resideInAnyPackage("..api..", "web.controller..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("persistence.repository..", "..infrastructure.persistence..")
                .check(applicationClasses);
    }

    @Test
    void applicationServicesDoNotDependOnHttpControllers() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..api..", "web.controller..")
                .check(applicationClasses);
    }

    @Test
    void contractsDoNotDependOnDeliveryOrPersistenceImplementation() {
        noClasses().that().resideInAPackage("..contracts..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "org.springframework.data..",
                        "..api..", "web..", "persistence..", "..infrastructure..")
                .check(applicationClasses);
    }

    @Test
    void persistenceRepositoriesDoNotDependOnDeliveryOrApplicationServices() {
        noClasses().that().resideInAPackage("persistence.repository..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("service..", "web..", "..api..", "..application..")
                .check(applicationClasses);
    }
}
