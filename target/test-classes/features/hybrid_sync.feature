@hybrid @e2e
Feature: UI and API Data Synchronisation
  As a QA engineer
  I want to verify that data created via UI is consistent with API responses
  And that API operations are reflected correctly in the UI

  @smoke @critical
  Scenario: Note created via UI appears in API response
    Given the user is logged in via UI
    And a valid API auth token is available
    When the user creates a note via UI with title "Sprint Planning" category "Work" description "Define Q3 goals"
    And the user calls GET /notes via API
    Then the API response should contain a note with title "Sprint Planning"
    And the API note category should be "Work"
    And the API note description should be "Define Q3 goals"
    And all fields should match exactly between UI and API

  @critical
  Scenario: Note deleted via API disappears from UI
    Given a note "Delete Me" exists in category "Personal"
    And a valid API auth token is available
    When the note "Delete Me" is deleted via the API
    And the user refreshes the Notes dashboard
    Then the note "Delete Me" should not be visible in the UI list
    And the total note count in the UI should decrease by 1

  @critical
  Scenario: API response time is under 2 seconds
    Given a valid API auth token is available
    When the user calls GET /notes via API
    Then the API response time should be less than 2000 milliseconds
    When the user creates a note via API with title "Perf Test Note"
    Then the API response time should be less than 2000 milliseconds
