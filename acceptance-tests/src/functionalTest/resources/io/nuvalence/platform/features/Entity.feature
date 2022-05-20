Feature: Entity Management

  Scenario: Successfully create an entity
    Given a valid schema definition JSON file
    And the schema is published to the API
    And a valid Entity JSON file
    When the entity is published to the API
    Then the api response should have status code 200

  Scenario: Successfully retrieve an entity by ID
    Given a valid schema definition JSON file
    And the schema is published to the API
    And a valid Entity JSON file
    And the entity is published to the API
    When the entity is requested by ID
    Then the api response should have status code 200
    And the entity schema should be "test-valid"
    And the entity should have attribute "input" set to value "value"

  Scenario: Fail to retrieve an entity by ID that doesn't exist
    When the entity with ID "ec57f6ed-73bf-414d-861a-fd5449ea675a" is requested
    Then the api response should have status code 404

  Scenario: Find all entities for a given schema
    Given the schema we will search for
    And there are 2 entities created for the search schema
    When a search is executed for the search schema
    Then the api response should have status code 200
    And the search result contains 2 entities.
    And the search result contains an entity with attribute "input" set to value "value-0"
    And the search result contains an entity with attribute "input" set to value "value-1"