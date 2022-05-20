package io.nuvalence.platform;

import io.cucumber.java.en.Then;
import io.nuvalence.platform.utils.cucumber.contexts.ScenarioContext;
import org.junit.jupiter.api.Assertions;

public class CommonStepDefinitions {
    private final ScenarioContext context;

    public CommonStepDefinitions(ScenarioContext context) {
        this.context = context;
    }

    @Then("the api response should have status code {int}")
    public void checkStatusCode(final int status) {
        Assertions.assertEquals(status, context.getLastResponseStatus());
    }
}
