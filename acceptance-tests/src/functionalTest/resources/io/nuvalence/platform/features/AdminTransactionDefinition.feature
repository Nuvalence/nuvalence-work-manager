Feature: Admin Transaction Definition Management

  Scenario: Successfully create a Transaction Definition
    Given a valid transaction definition JSON file
    When the transaction definition is published to the API
    Then the api response should have status code 200

  Scenario: Successfully retrieve a Transaction Definition by ID
    Given a valid transaction definition JSON file
    And the transaction definition is published to the API
    When the transaction definition is requested by ID
    Then the api response should have status code 200
    And the transaction definition name should be "test-valid"
    And the transaction definition contains a task-form mapping that maps task "test-task" to form "71185226-a4dc-4a34-b2a6-bac8f7e05a4b" for role "customer"
    And the transaction definition contains a named-form mapping that maps form name "test-form" to form "71185226-a4dc-4a34-b2a6-bac8f7e05a4b" for role "customer"

  Scenario: Fail to retrieve a Transaction Definition by ID that doesn't exist
    When the transaction definition with ID "ec57f6ed-73bf-414d-861a-fd5449ea675a" is requested
    Then the api response should have status code 404

  Scenario: Find existing Transaction Definition in search
    Given a valid transaction definition JSON file
    When the transaction definition is published to the API
    And a transaction definition search is executed for "test"
    Then the api response should have status code 200
    And the search results contain a transaction definition with name of "test-valid"