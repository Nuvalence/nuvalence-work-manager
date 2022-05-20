package io.nuvalence.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.nuvalence.platform.utils.cucumber.contexts.ScenarioContext;
import io.nuvalence.workmanager.client.ApiClient;
import io.nuvalence.workmanager.client.ApiException;
import io.nuvalence.workmanager.client.generated.controllers.TransactionApi;
import io.nuvalence.workmanager.client.generated.models.TransactionDefinitionModel;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class AdminTransactionDefinitionStepDefinitions {
    private final ScenarioContext context;
    private final TransactionApi client;
    private final ObjectMapper objectMapper;
    private TransactionDefinitionModel transactionDefinitionModel;
    private List<TransactionDefinitionModel> lastSearchResult;

    public AdminTransactionDefinitionStepDefinitions(ScenarioContext context) {
        this.context = context;
        var apiClient = new ApiClient();
        apiClient.updateBaseUri(context.getBaseUri());
        client = new TransactionApi(apiClient);
        objectMapper = new ObjectMapper();
    }

    @Given("a valid transaction definition JSON file")
    public void useDefaultValidTransactionDefinition() {
        try (
                InputStream res = this.getClass()
                        .getResourceAsStream("requests/transaction/default-valid-transaction-definition.json")
        ) {
            transactionDefinitionModel = objectMapper.readValue(res, TransactionDefinitionModel.class);
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    @When("the transaction definition is published to the API")
    public void publishTransactionDefinition() {
        transactionDefinitionModel = context.recordResponse(
            () -> client.postTransactionDefinitionWithHttpInfo(transactionDefinitionModel)
        );
        Assertions.assertEquals(200, context.getLastResponseStatus());
    }

    @When("the transaction definition with ID {string} is requested")
    public void getTransactionDefinition(String id) {
        transactionDefinitionModel = context.recordResponse(
            () -> client.getTransactionDefinitionWithHttpInfo(UUID.fromString(id))
        );
    }

    @When("the transaction definition is requested by ID")
    public void getTransactionDefinitionByLastRecordedId() {
        transactionDefinitionModel = context.recordResponse(
            () -> client.getTransactionDefinitionWithHttpInfo(transactionDefinitionModel.getId())
        );
    }

    @When("a transaction definition search is executed for {string}")
    public void executeSearch(String search) throws ApiException {
        lastSearchResult = context.recordResponse(() -> client.getTransactionDefinitionsWithHttpInfo(search));
    }

    @Then("the transaction definition name should be {string}")
    public void theTransactionDefinitionNameShouldBe(String name) {
        Assertions.assertEquals(name, transactionDefinitionModel.getName());
    }

    @Then("the search results contain a transaction definition with name of {string}")
    public void theSearchResultsContainATransactionDefinitionWithNameOf(String name) {
        Assertions.assertTrue(
                lastSearchResult.stream()
                        .anyMatch(transactionDefinition -> transactionDefinition.getName().equals(name))
        );
    }

    @And("the transaction definition contains a task-form mapping that maps task {string} to form {string} "
            + "for role {string}")
    public void assertTaskFormMappingPresent(String task, String form, String role) {
        Assertions.assertTrue(
                transactionDefinitionModel
                        .getTaskFormMappings()
                        .stream()
                        .anyMatch(mapping -> task.equals(mapping.getTaskDefinitionId())
                                && UUID.fromString(form).equals(mapping.getFormId())
                                && role.equals(mapping.getRole()))
        );
    }

    @And("the transaction definition contains a named-form mapping that maps form name {string} to form {string} "
            + "for role {string}")
    public void assertNamedFormMappingPresent(String formName, String formId, String role) {
        Assertions.assertTrue(
                transactionDefinitionModel
                        .getNamedFormMappings()
                        .stream()
                        .anyMatch(mapping -> formName.equals(mapping.getFormConfigName())
                                && UUID.fromString(formId).equals(mapping.getFormId())
                                && role.equals(mapping.getRole()))
        );
    }
}
