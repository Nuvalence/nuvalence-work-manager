Feature: Admin Form Config Management

  Scenario: Successfully create a Form Config
    Given a valid schema definition JSON file
    And the schema is published to the API
    And a valid form config JSON file
    When the form config is published to the API
    Then the api response should have status code 200

  Scenario: Successfully retrieve a Form Config by ID
    Given a valid schema definition JSON file
    And the schema is published to the API
    And a valid form config JSON file
    And the form config is published to the API
    When the form config is requested by ID
    Then the api response should have status code 200
    And the form config name should be "test-valid"

  Scenario: Fail to retrieve a form config by ID that doesn't exist
    When the form config with ID "ec57f6ed-73bf-414d-861a-fd5449ea675a" is requested
    Then the api response should have status code 404

  Scenario: Find existing form config in search
    Given a valid schema definition JSON file
    And the schema is published to the API
    And a valid form config JSON file
    When the form config is published to the API
    And a form config search is executed for "test"
    Then the api response should have status code 200
    And the search results contain a form config with name of "test-valid"