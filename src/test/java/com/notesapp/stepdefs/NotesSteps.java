package com.notesapp.stepdefs;

import com.notesapp.pages.NotesPage;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.testng.Assert;

import java.util.List;

public class NotesSteps {

    private NotesPage notesPage = new NotesPage();
    private int noteCountBefore;

    @Given("the user is on the Notes dashboard")
    public void onDashboard() {
        Assert.assertTrue(notesPage.isOnDashboard(), "User should be on Notes dashboard");
    }

    @When("the user clicks the Add Note button")
    public void clickAddNote() {
        notesPage.clickAddNote();
    }

    @When("the user selects category {string}")
    public void selectCategory(String category) {
        notesPage.selectCategory(category);
    }

    @When("the user enters title {string}")
    public void enterTitle(String title) {
        notesPage.enterTitle(title);
    }

    @When("the user enters description {string}")
    public void enterDescription(String desc) {
        notesPage.enterDescription(desc);
    }

    @When("the user saves the note")
    public void saveNote() {
        notesPage.saveNote();
    }

    @Then("the note {string} should appear in the notes list")
    public void noteIsPresent(String title) {
        Assert.assertTrue(notesPage.isNotePresent(title),
                "Note '" + title + "' was not found in the notes list");
    }

    @Then("the note category should show {string}")
    public void verifyCategoryDisplayed(String category) {
        List<String> cats = notesPage.getNoteCategoriesDisplayed();
        Assert.assertTrue(cats.stream().anyMatch(c -> c.equalsIgnoreCase(category)),
                "Category '" + category + "' not found in displayed categories: " + cats);
    }

    @When("the user creates a note with title {string} in category {string}")
    public void createNoteWithCategory(String title, String category) {
        notesPage.createNote(title, "Auto-generated note", category);
    }

    @Given("a note {string} exists in category {string}")
    public void noteExists(String title, String category) {
        if (!notesPage.isNotePresent(title)) {
            notesPage.createNote(title, "Precondition note", category);
        }
    }

    @When("the user opens the note {string}")
    public void openNote(String title) {
        notesPage.openNote(title);
    }

    @When("the user edits the title to {string}")
    public void editTitle(String newTitle) {
        notesPage.clearAndType(
                By.id("title"), newTitle);
    }

    @Then("the note {string} should no longer appear")
    public void noteIsAbsent(String title) {
        Assert.assertTrue(notesPage.isNoteAbsent(title),
                "Note '" + title + "' should not appear but was still found");
    }

    @When("the user deletes the note {string}")
    public void deleteNote(String title) {
        noteCountBefore = notesPage.getNoteCount();
        notesPage.deleteNote(title);
    }

    @Then("the note {string} should no longer appear in the notes list")
    public void noteAbsentFromList(String title) {
        Assert.assertTrue(notesPage.isNoteAbsent(title),
                "Deleted note '" + title + "' still visible in list");
    }

    @Then("a validation error should appear on the title field")
    public void titleValidationError() {
        Assert.assertTrue(notesPage.isTitleErrorDisplayed(),
                "Expected title field validation error but none was shown");
    }

    @Then("no new note should be created")
    public void noNewNoteCreated() {
        Assert.assertEquals(notesPage.getNoteCount(), noteCountBefore,
                "Note count changed — a note was incorrectly created");
    }

    @Given("notes exist in categories {string} and {string}")
    public void notesExistInCategories(String cat1, String cat2) {
        if (!notesPage.isNotePresent("Filter Test " + cat1)) {
            notesPage.createNote("Filter Test " + cat1, "desc", cat1);
        }
        if (!notesPage.isNotePresent("Filter Test " + cat2)) {
            notesPage.createNote("Filter Test " + cat2, "desc", cat2);
        }
    }

    @When("the user selects the category filter {string}")
    public void applyCategoryFilter(String category) {
        notesPage.filterByCategory(category);
    }

    @Then("only {string} category notes should be displayed")
    public void onlyCategoryShown(String category) {
        List<String> displayed = notesPage.getNoteCategoriesDisplayed();
        Assert.assertFalse(displayed.isEmpty(), "No notes displayed after filtering");
        for (String cat : displayed) {
            Assert.assertEquals(cat, category,
                    "Found a note with category '" + cat + "' while filter was set to '" + category + "'");
        }
    }
}
