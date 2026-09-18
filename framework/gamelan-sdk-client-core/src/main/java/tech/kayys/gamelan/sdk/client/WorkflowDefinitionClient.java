package tech.kayys.gamelan.sdk.client;

import java.util.List;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;

/**
 * Interface for workflow definition operations.
 * This interface is transport-agnostic, with implementations for REST, gRPC,
 * and Local.
 */
public interface WorkflowDefinitionClient extends AutoCloseable {
    /**
     * Creates a new workflow definition.
     *
     * @param request the definition details
     * @return a Uni containing the created definition details
     */
    Uni<WorkflowDefinition> createWorkflow(WorkflowDefinition request);

    /**
     * Retrieves a workflow definition by its unique identifier.
     *
     * @param definitionId the unique ID of the workflow definition
     * @return a Uni containing the definition details
     */
    Uni<WorkflowDefinition> getWorkflow(String definitionId);

    /**
     * Retrieves a workflow definition by its name.
     *
     * @param name the name of the workflow
     * @return a Uni containing the definition details
     */
    Uni<WorkflowDefinition> getWorkflowByName(String name);

    /**
     * Lists active workflow definitions for the current tenant.
     *
     * @return a Uni containing a list of definitions
     */
    Uni<List<WorkflowDefinition>> listWorkflows();

    /**
     * Lists workflow definitions for the current tenant.
     *
     * @param activeOnly whether inactive/soft-deleted definitions should be hidden
     * @return a Uni containing a list of definitions
     */
    default Uni<List<WorkflowDefinition>> listWorkflows(boolean activeOnly) {
        return listWorkflows();
    }

    /**
     * Retrieves a workflow definition by its name, version, and fingerprint.
     *
     * @param name the name of the workflow
     * @param version the version of the workflow
     * @param fingerprint the fingerprint of the workflow definition
     * @return a Uni containing the definition details, or null if not found
     */
    default Uni<WorkflowDefinition> getWorkflowByFingerprint(String name, String version, String fingerprint) {
        return listWorkflows(true).map(defs -> defs.stream()
                .filter(d -> (name == null || name.equals(d.name()))
                        && (version == null || version.equals(d.version()))
                        && d.metadata() != null && d.metadata().labels() != null
                        && fingerprint.equals(d.metadata().labels().get("wayang.coordination.fingerprint")))
                .findFirst()
                .orElse(null));
    }

    /**
     * Deletes a workflow definition.
     *
     * @param definitionId the unique ID of the workflow definition
     * @return a Uni representing completion
     */
    Uni<Void> deleteWorkflow(String definitionId);

    @Override
    void close();
}
