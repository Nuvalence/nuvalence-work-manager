package io.nuvalence.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.nuvalence.platform.utils.cucumber.contexts.ScenarioContext;
import io.nuvalence.workmanager.client.ApiClient;
import io.nuvalence.workmanager.client.ApiException;
import io.nuvalence.workmanager.client.generated.controllers.SchemaApi;
import io.nuvalence.workmanager.client.generated.models.SchemaModel;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Steps supporting Admin Schema Management features.
 */
public class AdminSchemaStepDefinitions {
    private final ScenarioContext context;
    private final SchemaApi client;
    private final ObjectMapper objectMapper;
    private SchemaModel schema;
    private List<SchemaModel> lastSearchResult;

    public AdminSchemaStepDefinitions(ScenarioContext context) {
        this.context = context;
        var apiClient = new ApiClient();
        apiClient.updateBaseUri(context.getBaseUri());
        client = new SchemaApi(apiClient);
        objectMapper = new ObjectMapper();
    }

    @Given("a valid schema definition JSON file")
    public void useDefaultValidSchema() {
        try (InputStream res = this.getClass().getResourceAsStream("requests/schema/default-valid-schema.json")) {
            schema =  objectMapper.readValue(res, SchemaModel.class);
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    @When("the schema is published to the API")
    public void publishSchema() {
        context.recordResponseWithNoBody(() -> client.postSchemaWithHttpInfo(schema));
        Assertions.assertEquals(204, context.getLastResponseStatus());
    }

    @When("the schema with name {string} is requested")
    public void getSchema(String name) {
        schema = context.recordResponse(() -> client.getSchemaWithHttpInfo(name));
    }

    @When("a schema search is executed for {string}")
    public void executeSearch(String search) throws ApiException {
        lastSearchResult = context.recordResponse(() -> client.getSchemasWithHttpInfo(search));
    }

    @Then("the search results contain a schema with name of {string}")
    public void theSearchResultsContainASchemaWithNameOf(String name) {
        Assertions.assertTrue(lastSearchResult.stream().anyMatch(schema -> schema.getName().equals(name)));
    }

    @Then("the schema name should be {string}")
    public void theSchemaNameShouldBe(String name) {
        Assertions.assertEquals(name, schema.getName());
    }
}
