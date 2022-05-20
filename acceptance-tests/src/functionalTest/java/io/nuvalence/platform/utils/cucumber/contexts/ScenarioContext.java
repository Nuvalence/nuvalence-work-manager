package io.nuvalence.platform.utils.cucumber.contexts;

import io.nuvalence.workmanager.client.ApiException;
import io.nuvalence.workmanager.client.ApiResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Optional;

/**
 * Shared context/data to be retained throughout a test scenario.
 * This is injected by cucumber-picocontainer in any step definitions
 * class which takes this as a constructor argument.
 */
@Getter
@Setter
@Slf4j
public class ScenarioContext {
    private static final String baseUri = Optional.ofNullable(System.getenv("SERVICE_URI"))
        .orElse("http://localhost:8080");

    private InputStream loadedResource;
    private ApiResponse<?> lastApiResponse;
    private Integer lastResponseStatus;

    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Executes the given call and records the response and status code.
     *
     * @param call Call to execute
     * @param <T> Type for response body
     * @return response body
     */
    public <T> T recordResponse(final ApiCall<T> call) {
        try {
            final ApiResponse<T> response = call.perform();
            lastApiResponse = response;
            lastResponseStatus = response.getStatusCode();
            return response.getData();
        } catch (ApiException e) {
            log.info(
                    "An error was encountered executing a API call. This may have been expected and part of this test: "
                            + e.getMessage()
            );
            lastApiResponse = null;
            lastResponseStatus = e.getCode();
            return null;
        }
    }

    /**
     * Executes the given call and records the response and status code.
     *
     * @param call Call to execute
     */
    public void recordResponseWithNoBody(final ApiCall<Void> call) {
        try {
            lastApiResponse = call.perform();
            lastResponseStatus = lastApiResponse.getStatusCode();
        } catch (ApiException e) {
            log.info(
                    "An error was encountered executing a API call. This may have been expected and part of this test.",
                    e
            );
            lastApiResponse = null;
            lastResponseStatus = e.getCode();
        }
    }

    /**
     * Functional interface that represents and API call that can be passed as a function parameter.
     *
     * @param <T> Response body type
     */
    @FunctionalInterface
    public static interface ApiCall<T> {
        /**
         * Executes the API call represented by this ApiCall.
         *
         * @return Response body
         * @throws ApiException If any API errors occur in this call
         */
        ApiResponse<T> perform() throws ApiException;
    }
}
