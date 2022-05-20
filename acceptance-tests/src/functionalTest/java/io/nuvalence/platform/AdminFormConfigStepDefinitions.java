package io.nuvalence.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.nuvalence.platform.utils.cucumber.contexts.ScenarioContext;
import io.nuvalence.workmanager.client.ApiClient;
import io.nuvalence.workmanager.client.ApiException;
import io.nuvalence.workmanager.client.generated.controllers.FormConfigApi;
import io.nuvalence.workmanager.client.generated.models.FormConfigDefinitionModel;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Steps supporting Admin Form Config Management features.
 */
public class AdminFormConfigStepDefinitions {
    private final ScenarioContext context;
    private final FormConfigApi client;
    private final ObjectMapper objectMapper;
    private FormConfigDefinitionModel formConfigModel;
    private List<FormConfigDefinitionModel> lastSearchResult;

    public AdminFormConfigStepDefinitions(ScenarioContext context) {
        this.context = context;
        var apiClient = new ApiClient();
        apiClient.updateBaseUri(context.getBaseUri());
        client = new FormConfigApi(apiClient);
        objectMapper = new ObjectMapper();
    }

    @Given("a valid form config JSON file")
    public void useDefaultValidFormConfig() {
        try (
                InputStream res = this.getClass()
                        .getResourceAsStream("requests/formconfig/default-valid-form-config.json")
        ) {
            formConfigModel = objectMapper.readValue(res, FormConfigDefinitionModel.class);
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    @When("the form config is published to the API")
    public void publishFormConfig() {
        formConfigModel = context.recordResponse(
            () -> client.postFormConfigWithHttpInfo(formConfigModel)
        );
        Assertions.assertEquals(200, context.getLastResponseStatus());
    }

    @When("the form config with ID {string} is requested")
    public void getFormConfig(String id) {
        formConfigModel = context.recordResponse(
            () -> client.getFormConfigByIdWithHttpInfo(UUID.fromString(id))
        );
    }

    @When("the form config is requested by ID")
    public void getFormConfigByLastRecordedId() {
        formConfigModel = context.recordResponse(
            () -> client.getFormConfigByIdWithHttpInfo(formConfigModel.getId())
        );
    }


    @When("a form config search is executed for {string}")
    public void executeSearch(String search) throws ApiException {
        lastSearchResult = context.recordResponse(() -> client.getFormConfigsWithHttpInfo(search));
    }

    @Then("the form config name should be {string}")
    public void theFormConfigNameShouldBe(String name) {
        Assertions.assertEquals(name, formConfigModel.getName());
    }

    @Then("the search results contain a form config with name of {string}")
    public void theSearchResultsContainAFormConfigWithNameOf(String name) {
        Assertions.assertTrue(
                lastSearchResult.stream()
                        .anyMatch(formConfigDefinition -> formConfigDefinition.getName().equals(name))
        );
    }
}
