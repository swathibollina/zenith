package com.pavan.zenith.catalog;

import com.pavan.zenith.testing.architecture.ZenithArchitectureRules;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(
    packages = "com.pavan.zenith.catalog",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest extends ZenithArchitectureRules {}
