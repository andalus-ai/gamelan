package tech.kayys.gamelan.engine.run;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.workflow.WorkflowRun;
import tech.kayys.gamelan.engine.workflow.WorkflowRunSnapshot;

/**
 * Maps workflow run domain state to the transport-neutral SDK response model.
 */
@ApplicationScoped
public class RunResponseMapper {

    public RunResponse toResponse(WorkflowRun run) {
        if (run == null) {
            return null;
        }

        WorkflowRunSnapshot snapshot = run.createSnapshot();
        Long durationMs = null;
        if (snapshot.startedAt() != null && snapshot.completedAt() != null) {
            durationMs = Duration.between(snapshot.startedAt(), snapshot.completedAt()).toMillis();
        }

        return RunResponse.builder()
                .runId(snapshot.id().value())
                .workflowId(snapshot.definitionId().value())
                .workflowVersion(snapshot.definitionVersion())
                .status(snapshot.status().name())
                .createdAt(snapshot.createdAt())
                .startedAt(snapshot.startedAt())
                .completedAt(snapshot.completedAt())
                .durationMs(durationMs)
                .nodesExecuted(snapshot.nodeExecutions().size())
                .outputs(snapshot.variables())
                .build();
    }
}
