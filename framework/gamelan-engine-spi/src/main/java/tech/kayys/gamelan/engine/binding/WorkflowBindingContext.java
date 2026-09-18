package tech.kayys.gamelan.engine.binding;

import java.util.Map;

/**
 * Context container used during binding resolution.
 */
public record WorkflowBindingContext(
        Map<String, Object> workflowInputs,
        Map<String, Object> workflowVariables,
        Map<String, Map<String, Object>> nodeOutputs,
        Map<String, Object> executionContext
) {

    public WorkflowBindingContext {
        workflowInputs = workflowInputs == null
                ? Map.of()
                : Map.copyOf(workflowInputs);

        workflowVariables = workflowVariables == null
                ? Map.of()
                : Map.copyOf(workflowVariables);

        nodeOutputs = nodeOutputs == null
                ? Map.of()
                : Map.copyOf(nodeOutputs);

        executionContext = executionContext == null
                ? Map.of()
                : Map.copyOf(executionContext);
    }
}
