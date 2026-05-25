@ui @login
Feature: User Authentication
  As a registered user of the Notes application
  I want to log in with my credentials
  So that I can access and manage my notes

  Background:
    Given the user is on the login page

  @smoke @positive
  Scenario: Successful login with valid credentials
    When the user enters email "omgdongare@gmail.com" and password "123456"
    And the user clicks the Login button
    Then the user should be redirected to the Notes dashboard
    And the username should be visible in the header

  @negative
  Scenario: Login fails with incorrect password
    When the user enters email "omgdongare@gmail.com" and password "wrongpass123"
    And the user clicks the Login button
    Then an error message "Incorrect email address or password" should be displayed
    And the user should remain on the login page

  @negative
  Scenario: Login fails with empty email field
    When the user enters email "" and password "123456"
    And the user clicks the Login button
    Then a validation error should appear on the email field

  @negative
  Scenario: Login fails with unregistered email
    When the user enters email "notregistered@test.com" and password "123456"
    And the user clicks the Login button
    Then an error message "Incorrect email address or password" should be displayed
