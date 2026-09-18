package tech.kayys.gamelan.engine.workflow;

import java.util.List;

import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.dto.CreateWorkflowDefinitionRequest;
import tech.kayys.gamelan.engine.workflow.dto.UpdateWorkflowDefinitionRequest;

/**
 * Workflow definition service interface
 */
public interface WorkflowDefinitionService {

        Uni<WorkflowDefinition> create(
                        CreateWorkflowDefinitionRequest request,
                        TenantId tenantId);

        Uni<WorkflowDefinition> get(
                        WorkflowDefinitionId id,
                        TenantId tenantId);

        Uni<List<WorkflowDefinition>> list(
                        TenantId tenantId,
                        boolean activeOnly);

        Uni<WorkflowDefinition> getByName(
                        String name,
                        TenantId tenantId);

        default Uni<WorkflowDefinition> getByFingerprint(
                        String name,
                        String version,
                        String fingerprint,
                        TenantId tenantId) {
                return list(tenantId, true).map(defs -> defs.stream()
                                .filter(def -> (name == null || name.equals(def.name()))
                                                && (version == null || version.equals(def.version()))
                                                && def.metadata() != null
                                                && def.metadata().labels() != null
                                                && fingerprint.equals(def.metadata().labels().get("wayang.coordination.fingerprint")))
                                .findFirst()
                                .orElse(null));
        }

        Uni<WorkflowDefinition> update(
                        WorkflowDefinitionId id,
                        UpdateWorkflowDefinitionRequest request,
                        TenantId tenantId);

        Uni<Void> delete(
                        WorkflowDefinitionId id,
                        TenantId tenantId);
}