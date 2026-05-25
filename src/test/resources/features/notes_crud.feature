@ui @notes
Feature: Notes CRUD Operations
  As a logged-in user
  I want to create, read, update, and delete notes
  So that I can manage my personal information

  Background:
    Given the user is logged in as "omgdongare@gmail.com" with password "123456"
    And the user is on the Notes dashboard

  @smoke @positive
  Scenario: Create a new note successfully
    When the user clicks the Add Note button
    And the user selects category "Home"
    And the user enters title "Grocery List"
    And the user enters description "Buy milk, eggs, and bread"
    And the user saves the note
    Then the note "Grocery List" should appear in the notes list
    And the note category should show "Home"

  @positive
  Scenario: Create notes in all three categories
    When the user creates a note with title "Home Task" in category "Home"
    Then the note "Home Task" should appear in the notes list
    When the user creates a note with title "Work Report" in category "Work"
    Then the note "Work Report" should appear in the notes list
    When the user creates a note with title "Personal Goal" in category "Personal"
    Then the note "Personal Goal" should appear in the notes list

  @positive
  Scenario: Edit an existing note
    Given a note "Sprint Planning" exists in category "Work"
    When the user opens the note "Sprint Planning"
    And the user edits the title to "Q3 Sprint Planning"
    And the user saves the note
    Then the note "Q3 Sprint Planning" should appear in the notes list
    And the note "Sprint Planning" should no longer appear

  @positive
  Scenario: Delete a note from the UI
    Given a note "Temp Note" exists in category "Personal"
    When the user deletes the note "Temp Note"
    Then the note "Temp Note" should no longer appear in the notes list

  @negative
  Scenario: Cannot create a note with empty title
    When the user clicks the Add Note button
    And the user selects category "Work"
    And the user enters title ""
    And the user enters description "Some description"
    And the user saves the note
    Then a validation error should appear on the title field
    And no new note should be created

  @negative
  Scenario: Filter notes by category
    Given notes exist in categories "Home" and "Work"
    When the user selects the category filter "Work"
    Then only "Work" category notes should be displayed
    When the user selects the category filter "Home"
    Then only "Home" category notes should be displayed
