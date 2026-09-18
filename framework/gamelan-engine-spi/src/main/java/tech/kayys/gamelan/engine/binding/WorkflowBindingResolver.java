package tech.kayys.gamelan.engine.binding;

import java.util.Map;

/**
 * Service for resolving workflow bindings against runtime context.
 */
public interface WorkflowBindingResolver {

    Object resolve(
            WorkflowBinding binding,
            WorkflowBindingContext context);

    Map<String, Object> resolveMap(
            Map<String, Object> values,
            WorkflowBindingContext context);
}
