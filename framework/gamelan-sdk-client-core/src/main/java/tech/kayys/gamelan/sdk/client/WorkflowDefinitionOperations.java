package tech.kayys.gamelan.sdk.client;

import java.util.List;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;

/**
 * Provides a fluent API for performing operations on workflow definitions.
 */
public class WorkflowDefinitionOperations {

    private final WorkflowDefinitionClient client;

    public WorkflowDefinitionOperations(WorkflowDefinitionClient client) {
        this.client = client;
    }

    /**
     * Initiates the creation of a new workflow definition.
     *
     * @param name the name of the workflow
     * @return a builder to configure and execute the definition creation
     */
    public WorkflowDefinitionBuilder create(String name) {
        return new WorkflowDefinitionBuilder(client, name);
    }

    /**
     * Retrieves a workflow definition by its unique identifier.
     *
     * @param definitionId the unique ID of the workflow definition
     * @return a Uni containing the definition details
     */
    public Uni<WorkflowDefinition> get(String definitionId) {
        return client.getWorkflow(definitionId);
    }

    /**
     * Retrieves a workflow definition by its name.
     *
     * @param name the name of the workflow
     * @return a Uni containing the definition details
     */
    public Uni<WorkflowDefinition> getByName(String name) {
        return client.getWorkflowByName(name);
    }

    /**
     * Lists active workflow definitions for the current tenant.
     *
     * @return a Uni containing a list of definitions
     */
    public Uni<List<WorkflowDefinition>> list() {
        return client.listWorkflows();
    }

    /**
     * Lists workflow definitions for the current tenant.
     *
     * @param activeOnly whether inactive/soft-deleted definitions should be hidden
     * @return a Uni containing a list of definitions
     */
    public Uni<List<WorkflowDefinition>> list(boolean activeOnly) {
        return client.listWorkflows(activeOnly);
    }

    /**
     * Lists active and inactive workflow definitions for lifecycle administration.
     *
     * @return a Uni containing a list of definitions
     */
    public Uni<List<WorkflowDefinition>> listAll() {
        return client.listWorkflows(false);
    }

    /**
     * Deletes a workflow definition.
     *
     * @param definitionId the unique ID of the workflow definition
     * @return a Uni representing the completion of the deletion
     */
    public Uni<Void> delete(String definitionId) {
        return client.deleteWorkflow(definitionId);
    }

    /**
     * Retrieves a workflow definition matching the given name, version, and fingerprint.
     *
     * @param name workflow name
     * @param version workflow version
     * @param fingerprint SHA-256 fingerprint
     * @return a Uni containing the workflow definition or null
     */
    public Uni<WorkflowDefinition> getByFingerprint(String name, String version, String fingerprint) {
        return client.getWorkflowByFingerprint(name, version, fingerprint);
    }

    /**
     * Creates a workflow definition directly from an immutable model instance.
     *
     * @param definition the definition to create
     * @return a Uni containing the created definition
     */
    public Uni<WorkflowDefinition> create(WorkflowDefinition definition) {
        return client.createWorkflow(definition);
    }

    /**
     * @return underlying WorkflowDefinitionClient
     */
    public WorkflowDefinitionClient client() {
        return client;
    }
}
