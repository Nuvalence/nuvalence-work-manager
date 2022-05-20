package io.nuvalence.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.nuvalence.platform.utils.cucumber.contexts.ScenarioContext;
import io.nuvalence.workmanager.client.ApiClient;
import io.nuvalence.workmanager.client.ApiException;
import io.nuvalence.workmanager.client.generated.controllers.EntityApi;
import io.nuvalence.workmanager.client.generated.controllers.SchemaApi;
import io.nuvalence.workmanager.client.generated.models.AttributeDefinitionModel;
import io.nuvalence.workmanager.client.generated.models.EntityModel;
import io.nuvalence.workmanager.client.generated.models.SchemaModel;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Steps supporting Entity features.
 */
public class EntityStepDefinitions {
    private final ScenarioContext context;
    private final EntityApi client;
    private final SchemaApi schemaClient;
    private final ObjectMapper objectMapper;
    private EntityModel entity;
    private String searchSchemaName;
    private List<EntityModel> lastSearchResult;

    public EntityStepDefinitions(ScenarioContext context) {
        this.context = context;
        var apiClient = new ApiClient();
        apiClient.updateBaseUri(context.getBaseUri());
        client = new EntityApi(apiClient);
        schemaClient = new SchemaApi(apiClient);
        objectMapper = new ObjectMapper();
    }

    @Given("a valid Entity JSON file")
    public void useDefaultValidEntity() {
        try (InputStream res = this.getClass().getResourceAsStream("requests/entity/default-valid-entity.json")) {
            entity = objectMapper.readValue(res, EntityModel.class);
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    @Given("the schema we will search for")
    public void theSchemaWeWillSearchFor() {
        searchSchemaName = UUID.randomUUID().toString();
        try {
            schemaClient.postSchema(
                    new SchemaModel()
                            .name(searchSchemaName)
                            .addAttributesItem(
                                    new AttributeDefinitionModel()
                                            .name("input")
                                            .type("String")
                            )
            );
        } catch (ApiException e) {
            Assertions.fail(e);
        }
    }

    @Given("there are {int} entities created for the search schema")
    public void thereAreEntitiesCreatedForTheSearchSchema(int count) {
        try {
            for (int i = 0; i < count; i++) {
                client.postEntity(
                        new EntityModel()
                                .schema(searchSchemaName)
                                .data(Map.of(
                                        "input", "value-" + i
                                ))
                );
            }
        } catch (ApiException e) {
            Assertions.fail(e);
        }
    }

    @When("the entity is published to the API")
    public void publishEntity() {
        entity = context.recordResponse(() -> client.postEntityWithHttpInfo(entity));
        Assertions.assertEquals(200, context.getLastResponseStatus());
    }

    @When("the entity is requested by ID")
    public void getEntityDefinitionByLastRecordedId() {
        entity = context.recordResponse(() -> client.getEntityWithHttpInfo(entity.getId()));
    }

    @When("the entity with ID {string} is requested")
    public void getEntityById(String id) {
        entity = context.recordResponse(() -> client.getEntityWithHttpInfo(UUID.fromString(id)));
    }

    @When("a search is executed for the search schema")
    public void entitySearch() {
        lastSearchResult = context.recordResponse(() -> client.getEntitiesBySchemaWithHttpInfo(searchSchemaName));
    }

    @Then("the entity schema should be {string}")
    public void entityHasSchema(String schema) {
        Assertions.assertEquals(schema, entity.getSchema());
    }

    @Then("the entity should have attribute {string} set to value {string}")
    public void theEntityShouldHaveAttributeSetToValue(String name, String value) {
        Assertions.assertNotNull(entity.getData().get(name));
        Assertions.assertEquals(value, entity.getData().get(name));
    }

    @And("the search result contains {int} entities.")
    public void theSearchResultContainsEntities(int size) {
        Assertions.assertEquals(size, lastSearchResult.size());
    }

    @And("the search result contains an entity with attribute {string} set to value {string}")
    public void theSearchResultContainsAnEntityWithAttributeSetToValue(String name, String value) {
        Assertions.assertTrue(
                lastSearchResult.stream()
                        .anyMatch(result -> result.getData().get(name) != null
                                && result.getData().get(name).equals(value))
        );
    }
}
