package tech.kayys.gamelan.sdk.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.Vertx;

class RestWorkflowRunClientTest {

    private WireMockServer wireMockServer;
    private Vertx vertx;
    private RestWorkflowRunClient client;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        vertx = Vertx.vertx();

        GamelanClientConfig config = GamelanClientConfig.builder()
                .endpoint("http://localhost:" + wireMockServer.port())
                .tenantId("test-tenant")
                .apiKey("test-api-key")
                .build();
        client = new RestWorkflowRunClient(config, vertx);
    }

    @AfterEach
    void tearDown() {
        client.close();
        vertx.closeAndAwait();
        wireMockServer.stop();
    }

    @Test
    void signalSendsIdempotencyKeyOutsidePayload() {
        stubFor(post(urlEqualTo("/api/v1/workflow-runs/run-1/signal"))
                .willReturn(aResponse().withStatus(204)));

        client.signal("run-1", "approved", "node-1", Map.of("approval.result", "yes"), "signal-1")
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertCompleted();

        verify(postRequestedFor(urlEqualTo("/api/v1/workflow-runs/run-1/signal"))
                .withHeader("X-Tenant-ID", equalTo("test-tenant"))
                .withHeader("Authorization", equalTo("Bearer test-api-key"))
                .withRequestBody(matchingJsonPath("$.signalName", equalTo("approved")))
                .withRequestBody(matchingJsonPath("$.targetNodeId", equalTo("node-1")))
                .withRequestBody(matchingJsonPath("$.idempotencyKey", equalTo("signal-1")))
                .withRequestBody(containing("\"approval.result\":\"yes\"")));
    }
    @Test
    void queryRunsUsesRuntimeDefinitionIdParameter() {
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
                com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo("/api/v1/workflow-runs"))
                .withQueryParam("page", equalTo("2"))
                .withQueryParam("size", equalTo("7"))
                .withQueryParam("definitionId", equalTo("workflow-1"))
                .withQueryParam("status", equalTo("RUNNING"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody("[]")));

        client.queryRuns("workflow-1", "RUNNING", 2, 7)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertItem(java.util.List.of())
                .assertCompleted();

        verify(com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
                com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo("/api/v1/workflow-runs"))
                .withQueryParam("definitionId", equalTo("workflow-1"))
                .withQueryParam("status", equalTo("RUNNING")));
    }

    @Test
    void activeRunCountUsesRuntimeActiveCountEndpoint() {
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
                urlEqualTo("/api/v1/workflow-runs/active-count"))
                .willReturn(aResponse().withStatus(200).withBody("3")));

        client.getActiveRunsCount()
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertItem(3L)
                .assertCompleted();

        verify(com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
                urlEqualTo("/api/v1/workflow-runs/active-count")));
    }

}
