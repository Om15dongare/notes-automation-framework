package com.notesapp.tests.ui;

import com.notesapp.base.BaseTest;
import com.notesapp.config.ConfigReader;
import com.notesapp.pages.LoginPage;
import com.notesapp.pages.NotesPage;
import io.qameta.allure.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.List;

@Epic("Notes App")
@Feature("UI — Category Filtering")
public class CategoryFilterTest extends BaseTest {

    private NotesPage notesPage;
    private final long ts = System.currentTimeMillis();

    /**
     * CategoryFilterTest needs to create 3 notes in sequence.
     * Chrome 148's renderer crashes under this load in headed mode,
     * so we force headless for this test class.
     */
    @Override
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chrome") String browser, @Optional("true") String headless) {
        // Force headless to prevent renderer timeout when seeding 3 notes
        super.setUp(browser, "true");
    }

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUp")
    public void loginAndSeedData() {
        notesPage = new LoginPage().loginAs(
                ConfigReader.getUserEmail(),
                ConfigReader.getUserPassword());

        // Seed one note per category — 2.5s between creates lets Chrome renderer fully settle
        notesPage.createNote("Home Note " + ts, "Home category note", "Home");
        notesPage.sleepMs(2500);
        notesPage.createNote("Work Note " + ts, "Work category note", "Work");
        notesPage.sleepMs(2500);
        notesPage.createNote("Personal Note " + ts, "Personal category note", "Personal");
        notesPage.sleepMs(1000);
    }

    @Test(description = "TC-UI-05: Filter notes by category — only matching notes shown")
    @Story("FR-03")
    @Severity(SeverityLevel.CRITICAL)
    public void testCategoryFilter() {
        // Allow Chrome renderer to fully settle after seeding 3 notes in @BeforeMethod
        notesPage.sleepMs(3000);

        notesPage.filterByCategory("Work");
        List<String> workCats = notesPage.getNoteCategoriesDisplayed();
        workCats.forEach(cat ->
                Assert.assertEquals(cat, "Work",
                        "After Work filter — found a note with category: " + cat));

        notesPage.filterByCategory("Home");
        List<String> homeCats = notesPage.getNoteCategoriesDisplayed();
        homeCats.forEach(cat ->
                Assert.assertEquals(cat, "Home",
                        "After Home filter — found a note with category: " + cat));

        log.info("TC-UI-05 PASSED — Category filter working correctly");
    }
}
