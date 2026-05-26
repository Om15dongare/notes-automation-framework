package com.notesapp.cucumber;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * CucumberRunner — bridges Cucumber 7 with TestNG via AbstractTestNGCucumberTests.
 *
 * How it works:
 *   • Extends AbstractTestNGCucumberTests, which is a TestNG test class that
 *     discovers and runs Cucumber scenarios as individual @Test methods.
 *   • Maven Surefire picks this up through testng-cucumber.xml.
 *   • Allure integration is handled by allure-cucumber7-jvm (already in pom.xml).
 *
 * @CucumberOptions:
 *   features  — path to .feature files inside src/test/resources
 *   glue      — packages containing Step Definitions and Hooks
 *   plugin    — Allure + pretty console + HTML report
 *   tags      — override at runtime: mvn test -Dcucumber.filter.tags="@smoke"
 *   monochrome — clean console output
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue     = {
                "com.notesapp.cucumber",   // Hooks.java lives here
                "com.notesapp.stepdefs"    // LoginSteps, NotesSteps, ApiSteps
        },
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "html:target/cucumber-reports/cucumber-report.html",
                "json:target/cucumber-reports/cucumber-report.json"
        },
        monochrome = true,
        // Default: run everything.  Override with -Dcucumber.filter.tags="@smoke"
        tags = "@ui or @hybrid or @e2e or @api"
)
public class CucumberRunner extends AbstractTestNGCucumberTests {

    /**
     * Run each Cucumber scenario as a separate TestNG data-row.
     * parallel=false keeps the single-threaded execution that the existing
     * TestNG suite also uses (avoiding shared-account race conditions).
     *
     * To enable parallel Cucumber execution later, set parallel=true and
     * ensure DriverManager's ThreadLocal is safe (it already is).
     */
    @DataProvider(parallel = false)
    @Override
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
