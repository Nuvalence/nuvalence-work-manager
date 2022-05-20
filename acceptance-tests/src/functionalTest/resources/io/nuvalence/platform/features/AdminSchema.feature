Feature: Admin Schema Management

  Scenario: Successfully create a schema
    Given a valid schema definition JSON file
    When the schema is published to the API
    Then the api response should have status code 204

  Scenario: Successfully retrieve a schema by name
    Given a valid schema definition JSON file
    When the schema is published to the API
    And the schema with name "test-valid" is requested
    Then the api response should have status code 200
    And the schema name should be "test-valid"

  Scenario: Fail to retrieve a schema by name that doesn't exist
    When the schema with name "missing-schema" is requested
    Then the api response should have status code 404

  Scenario: Find existing schema in search
    Given a valid schema definition JSON file
    When the schema is published to the API
    And a schema search is executed for "test"
    Then the api response should have status code 200
    And the search results contain a schema with name of "test-valid"