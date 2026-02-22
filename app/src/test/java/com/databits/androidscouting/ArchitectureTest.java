package com.databits.androidscouting;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import com.tngtech.archunit.ArchConfiguration;
import org.junit.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

public class ArchitectureTest {

    static {
        ArchConfiguration.get().setProperty("archRule.failOnEmptyShould", "false");
    }

    private final JavaClasses importedClasses = new ClassFileImporter().importPackages("com.databits.androidscouting");

    @Test
    public void viewModelsShouldNotAccessDaosDirectly() {
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("ViewModel")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Dao");

        rule.check(importedClasses);
    }

    @Test
    public void viewModelsShouldNotDependOnAndroidViews() {
        // ViewModels should be UI agnostic
        ArchRule rule = noClasses()
                .that().haveSimpleNameEndingWith("ViewModel")
                .should().dependOnClassesThat().resideInAPackage("android.view..")
                .orShould().dependOnClassesThat().resideInAPackage("android.widget..");

        // Note: QrCodeViewModel might require exemption if it handles Rect or similar android specific non-view classes
        // but it shouldn't hold references to Views.
        // We catch strict View/Widget dependencies here.
        rule.check(importedClasses);
    }

    @Test
    public void scannerShouldNotDependOnRepositoryImplementations() {
        ArchRule rule = noClasses()
                .that().haveSimpleName("Scanner")
                .should().dependOnClassesThat().resideInAPackage("..repository.impl..");

        rule.check(importedClasses);
    }

    @Test
    public void viewModelsShouldDependOnNarrowStoresNotConcreteRepository() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..viewmodel..")
                .and().haveSimpleNameEndingWith("ViewModel")
                .should().dependOnClassesThat().haveSimpleName("DefaultPreferenceRepository");

        rule.check(importedClasses);
    }

    @Test
    public void fragmentsShouldNotDependOnRepositoryImplementations() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..fragment..")
                .should().dependOnClassesThat().resideInAPackage("..data..impl..");

        rule.check(importedClasses);
    }

    @Test
    public void viewModelsShouldNotDependOnRepositoryGraphsOrProviders() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..viewmodel..")
                .and().haveSimpleNameEndingWith("ViewModel")
                .should().dependOnClassesThat().haveSimpleName("PreferenceRepositoryProvider")
                .orShould().dependOnClassesThat().haveSimpleName("AppRepositories")
                .orShould().dependOnClassesThat().resideInAPackage("..data.repository.impl..");

        rule.check(importedClasses);
    }

    @Test
    public void viewModelsShouldDependOnStoreContractsNotRepositoryContracts() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..viewmodel..")
                .and().haveSimpleNameEndingWith("ViewModel")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

        rule.check(importedClasses);
    }

    @Test
    public void adapterGatewaysShouldUseStoreNaming() {
        ArchRule rule = classes()
                .that().resideInAPackage("..data.repository.adapter..")
                .and().areTopLevelClasses()
                .should().haveSimpleNameStartingWith("Store");

        rule.check(importedClasses);
    }

    @Test
    public void repositoriesShouldBeTheOnlyOnesAccessingDaos() {
        // Enforce repository pattern
        // DAOs should only be accessed by Repositories, Databases (creation), or Factories
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Dao")
                .should().onlyBeAccessed().byClassesThat().haveSimpleNameEndingWith("Repository")
                .orShould().onlyBeAccessed().byClassesThat().haveSimpleNameEndingWith("Database")
                .orShould().onlyBeAccessed().byClassesThat().haveNameMatching(".*Test.*");

        rule.check(importedClasses);
    }

    @Test
    public void fragmentsShouldNotAccessDaosDirectly() {
        // UI Layer (Fragments) should never touch the database directly
        ArchRule rule = noClasses()
                .that().resideInAPackage("..fragment..")
                .should().accessClassesThat().haveSimpleNameEndingWith("Dao");

        rule.check(importedClasses);
    }

    @Test
    public void noCyclesBetweenSlices() {
        // Prevent circular dependencies between top-level packages
        ArchRule rule = slices()
                .matching("com.databits.androidscouting.(*)..")
                .should().beFreeOfCycles();

        rule.check(importedClasses);
    }

    @Test
    public void namingConventions() {
        // Enforce naming standards
        ArchRule viewModels = classes()
                .that().resideInAPackage("..viewmodel..")
                .and().areTopLevelClasses()
                .and().doNotHaveSimpleName("CameraSettingsViewModelFactory")
                .and().doNotHaveSimpleName("ProvisionViewModelFactory")
                .and().doNotHaveSimpleName("SyncStatusViewModelFactory")
                .and().doNotHaveSimpleName("BaseViewModelTest") // Test helper exception
                .should().haveSimpleNameEndingWith("ViewModel");

        ArchRule repositories = classes()
                .that().resideInAPackage("..repository..")
                .and().areTopLevelClasses()
                .should().haveSimpleNameEndingWith("Repository");

        viewModels.check(importedClasses);
        repositories.check(importedClasses);
    }

    @Test
    public void noStandardOutputLogging() {
        // Use Android Log.d/e/i instead of System.out.println
        GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(importedClasses);
    }
}
