package tech.kayys.gamelan.engine.binding;

import java.util.Objects;

/**
 * Serializable specification of a workflow binding for transport and persistence.
 */
public record WorkflowBindingSpec(
        String source,
        String path,
        Object value
) {

    public WorkflowBindingSpec {
        Objects.requireNonNull(source, "source must not be null");
    }

    public WorkflowBinding toBinding() {
        WorkflowBinding.BindingSource bindingSource =
                WorkflowBinding.BindingSource.valueOf(source.toUpperCase());

        return new WorkflowBinding(
                bindingSource,
                path,
                value);
    }

    public static WorkflowBindingSpec from(WorkflowBinding binding) {
        return new WorkflowBindingSpec(
                binding.source().name(),
                binding.path(),
                binding.value());
    }
}
