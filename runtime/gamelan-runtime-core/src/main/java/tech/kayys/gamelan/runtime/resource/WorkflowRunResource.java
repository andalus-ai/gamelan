package tech.kayys.gamelan.runtime.resource;

import java.util.List;
import java.util.Map;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import tech.kayys.gamelan.security.GamelanConfig;
import tech.kayys.gamelan.engine.context.RequestContext;
import tech.kayys.gamelan.engine.execution.ExecutionHistory;
import tech.kayys.gamelan.engine.run.CreateRunRequest;
import tech.kayys.gamelan.engine.run.RunResponse;
import tech.kayys.gamelan.engine.run.RunResponseMapper;
import tech.kayys.gamelan.engine.run.RunStatus;
import tech.kayys.gamelan.engine.run.dto.ResumeRunRequest;
import tech.kayys.gamelan.engine.signal.Signal;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionId;
import tech.kayys.gamelan.engine.workflow.WorkflowRun;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.gamelan.engine.workflow.WorkflowRunManager;
import tech.kayys.gamelan.engine.workflow.WorkflowRunSnapshot;

@Path("/api/v1/workflow-runs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkflowRunResource {

    @Inject
    WorkflowRunManager runManager;

    @Inject
    RunResponseMapper responseMapper;

    @Inject
    RequestContext requestContext;

    @Inject
    GamelanConfig config;

    private TenantId tenant() {
        return requestContext.getTenantId().orElseGet(config::getDefaultTenant);
    }

    @POST
    public Uni<RunResponse> create(CreateRunRequest request) {
        request.setTenantId(tenant());
        return runManager.createRun(request).map(responseMapper::toResponse);
    }

    @GET
    @Path("/{id}")
    public Uni<RunResponse> get(@PathParam("id") String id) {
        return runManager.getRun(WorkflowRunId.of(id), tenant()).map(responseMapper::toResponse);
    }

    @GET
    @Path("/{id}/snapshot")
    public Uni<WorkflowRunSnapshot> getSnapshot(@PathParam("id") String id) {
        return runManager.getSnapshot(WorkflowRunId.of(id), tenant());
    }

    @GET
    @Path("/{id}/history")
    public Uni<ExecutionHistory> getHistory(@PathParam("id") String id) {
        return runManager.getExecutionHistory(WorkflowRunId.of(id), tenant());
    }

    @POST
    @Path("/{id}/start")
    public Uni<RunResponse> start(@PathParam("id") String id) {
        return runManager.startRun(WorkflowRunId.of(id), tenant()).map(responseMapper::toResponse);
    }

    @POST
    @Path("/{id}/suspend")
    public Uni<RunResponse> suspend(@PathParam("id") String id, Map<String, Object> params) {
        String reason = (String) params.getOrDefault("reason", "Manual suspension");
        String waitingOnNodeId = (String) params.get("waitingOnNodeId");
        tech.kayys.gamelan.engine.node.NodeId waitingNode = waitingOnNodeId != null && !waitingOnNodeId.isBlank()
                ? tech.kayys.gamelan.engine.node.NodeId.of(waitingOnNodeId.trim())
                : null;
        return runManager.suspendRun(WorkflowRunId.of(id), tenant(), reason, waitingNode)
                .map(responseMapper::toResponse);
    }

    @POST
    @Path("/{id}/resume")
    public Uni<RunResponse> resume(@PathParam("id") String id, ResumeRunRequest request) {
        return runManager.resumeRun(WorkflowRunId.of(id), tenant(), request.resumeData(), request.humanTaskId())
                .map(responseMapper::toResponse);
    }

    @POST
    @Path("/{id}/cancel")
    public Uni<Void> cancel(@PathParam("id") String id, Map<String, Object> params) {
        String reason = (String) params.getOrDefault("reason", "Manual cancellation");
        return runManager.cancelRun(WorkflowRunId.of(id), tenant(), reason);
    }

    @GET
    public Uni<List<RunResponse>> query(
            @QueryParam("definitionId") String definitionId,
            @QueryParam("status") RunStatus status,
            @QueryParam("page") @jakarta.ws.rs.DefaultValue("0") int page,
            @QueryParam("size") @jakarta.ws.rs.DefaultValue("10") int size) {
        WorkflowDefinitionId wfDefId = definitionId != null ? new WorkflowDefinitionId(definitionId) : null;
        return runManager.queryRuns(tenant(), wfDefId, status, page, size)
                .map(runs -> runs.stream().map(responseMapper::toResponse).toList());
    }

    @GET
    @Path("/active-count")
    public Uni<Long> getActiveCount() {
        return runManager.getActiveRunsCount(tenant());
    }

    @POST
    @Path("/{id}/signal")
    @SuppressWarnings("unchecked")
    public Uni<Void> signal(@PathParam("id") String id, Map<String, Object> payload) {
        String name = (String) payload.getOrDefault("signalName", payload.get("name"));
        String targetNodeIdStr = (String) payload.get("targetNodeId");
        Map<String, Object> data = (Map<String, Object>) payload.get("payload");
        String idempotencyKey = (String) payload.get("idempotencyKey");
        tech.kayys.gamelan.engine.node.NodeId targetNodeId = targetNodeIdStr != null && !targetNodeIdStr.isBlank()
                ? tech.kayys.gamelan.engine.node.NodeId.of(targetNodeIdStr.trim())
                : null;
        return runManager.signal(
                WorkflowRunId.of(id),
                tenant(),
                new Signal(name, targetNodeId, data, java.time.Instant.now(), idempotencyKey));
    }
}
