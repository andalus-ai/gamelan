package tech.kayys.gamelan.engine.binding;

import java.util.Objects;

/**
 * First-class workflow data binding descriptor.
 */
public record WorkflowBinding(
        BindingSource source,
        String path,
        Object value
) {

    public WorkflowBinding {
        Objects.requireNonNull(source, "source must not be null");

        if (source.requiresPath()
                && (path == null || path.isBlank())) {
            throw new IllegalArgumentException(
                    "path is required for " + source);
        }
    }

    public enum BindingSource {
        STATIC(false),
        WORKFLOW_INPUT(true),
        WORKFLOW_VARIABLE(true),
        NODE_OUTPUT(true),
        EXECUTION_CONTEXT(true);

        private final boolean requiresPath;

        BindingSource(boolean requiresPath) {
            this.requiresPath = requiresPath;
        }

        public boolean requiresPath() {
            return requiresPath;
        }
    }

    public static WorkflowBinding staticValue(Object value) {
        return new WorkflowBinding(
                BindingSource.STATIC,
                null,
                value);
    }

    public static WorkflowBinding workflowInput(String path) {
        return new WorkflowBinding(
                BindingSource.WORKFLOW_INPUT,
                path,
                null);
    }

    public static WorkflowBinding workflowVariable(String path) {
        return new WorkflowBinding(
                BindingSource.WORKFLOW_VARIABLE,
                path,
                null);
    }

    public static WorkflowBinding nodeOutput(String path) {
        return new WorkflowBinding(
                BindingSource.NODE_OUTPUT,
                path,
                null);
    }

    public static WorkflowBinding executionContext(String path) {
        return new WorkflowBinding(
                BindingSource.EXECUTION_CONTEXT,
                path,
                null);
    }
}
