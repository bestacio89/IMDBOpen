package com.example.demo.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class LayerDependencyArchitectureTest {
    private static JavaClasses applicationClasses;

    @BeforeAll
    static void importApplicationClasses() {
        applicationClasses = new ClassFileImporter()
                .importPackages("entities", "service", "persistence", "web",
                        "exceptions", "utilities", "com.example.demo");
    }

    @Test
    void domainEntitiesDoNotDependOnApplicationOrDeliveryLayers() {
        noClasses()
                .that().resideInAPackage("..entities..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("service..", "web..", "persistence..")
                .check(applicationClasses);
    }

    @Test
    void controllersDoNotDependOnPersistenceRepositories() {
        noClasses()
                .that().resideInAPackage("web.controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("persistence.repository..")
                .check(applicationClasses);
    }

    @Test
    void applicationServicesDoNotDependOnHttpControllers() {
        noClasses()
                .that().resideInAPackage("service..")
                .should().dependOnClassesThat()
                .resideInAPackage("web.controller..")
                .check(applicationClasses);
    }

    @Test
    void persistenceRepositoriesDoNotDependOnDeliveryOrApplicationServices() {
        noClasses()
                .that().resideInAPackage("persistence.repository..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("service..", "web..")
                .check(applicationClasses);
    }
}
