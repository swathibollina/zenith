package com.pavan.zenith.testing.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

public abstract class ZenithArchitectureRules {

  @ArchTest
  static final ArchRule DOMAIN_DEPENDS_ON_NOTHING_OUTSIDE_ITSELF =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..application..",
              "..adapter..",
              "..config..",
              "org.springframework..",
              "jakarta..",
              "org.hibernate..")
          .because("domain must stay free of frameworks and outer layers")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule APPLICATION_MAY_NOT_DEPEND_ON_ADAPTER =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..adapter..")
          .because("use-cases must not know about delivery or persistence details")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule JPA_ENTITIES_STAY_IN_PERSISTENCE_ADAPTER =
      classes()
          .that()
          .areAnnotatedWith("jakarta.persistence.Entity")
          .should()
          .resideInAPackage("..adapter.out.persistence..")
          .because("database rows are not domain objects and never cross the adapter boundary")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule NO_FIELD_INJECTION =
      NO_CLASSES_SHOULD_USE_FIELD_INJECTION
          .because("constructor injection makes dependencies explicit and testable")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule NO_PUBLIC_SETTERS_ON_AGGREGATES =
      noMethods()
          .that()
          .areDeclaredInClassesThat()
          .resideInAPackage("..domain.model..")
          .and()
          .arePublic()
          .should()
          .haveNameMatching("set[A-Z].*")
          .because(
              "state changes go through intention-named methods: event.publish(), not event.setStatus()")
          .allowEmptyShould(true);
}
