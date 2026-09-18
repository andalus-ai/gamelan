package tech.kayys.gamelan.core.binding;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.binding.WorkflowBinding;
import tech.kayys.gamelan.engine.binding.WorkflowBindingContext;
import tech.kayys.gamelan.engine.binding.WorkflowBindingResolver;

@ApplicationScoped
public class DefaultWorkflowBindingResolver implements WorkflowBindingResolver {

    @Override
    public Object resolve(
            WorkflowBinding binding,
            WorkflowBindingContext context) {

        Objects.requireNonNull(binding, "binding must not be null");
        Objects.requireNonNull(context, "context must not be null");

        return switch (binding.source()) {
            case STATIC -> binding.value();

            case WORKFLOW_INPUT -> resolvePath(
                    context.workflowInputs(),
                    binding.path());

            case WORKFLOW_VARIABLE -> resolvePath(
                    context.workflowVariables(),
                    binding.path());

            case NODE_OUTPUT -> resolveNodeOutput(
                    context,
                    binding.path());

            case EXECUTION_CONTEXT -> resolvePath(
                    context.executionContext(),
                    binding.path());
        };
    }

    @Override
    public Map<String, Object> resolveMap(
            Map<String, Object> values,
            WorkflowBindingContext context) {

        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> resolved = new LinkedHashMap<>();

        values.forEach((key, value) ->
                resolved.put(key, resolveValue(value, context)));

        return Map.copyOf(resolved);
    }

    private Object resolveValue(
            Object value,
            WorkflowBindingContext context) {

        if (value instanceof WorkflowBinding binding) {
            return resolve(binding, context);
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();

            map.forEach((key, nestedValue) ->
                    nested.put(String.valueOf(key), resolveValue(nestedValue, context)));

            return Map.copyOf(nested);
        }

        if (value instanceof Iterable<?> iterable) {
            return java.util.stream.StreamSupport
                    .stream(iterable.spliterator(), false)
                    .map(item -> resolveValue(item, context))
                    .toList();
        }

        return value;
    }

    private Object resolveNodeOutput(
            WorkflowBindingContext context,
            String path) {

        int separator = path.indexOf('.');
        if (separator <= 0) {
            throw new IllegalArgumentException(
                    "Node output path must be '<nodeId>.<path>': " + path);
        }

        String nodeId = path.substring(0, separator);
        String outputPath = path.substring(separator + 1);

        Map<String, Object> output = context.nodeOutputs().get(nodeId);
        if (output == null) {
            throw new IllegalArgumentException(
                    "No output found for node: " + nodeId);
        }

        return resolvePath(output, outputPath);
    }

    private Object resolvePath(
            Map<String, Object> source,
            String path) {

        if (path == null || path.isBlank()) {
            return source;
        }

        Object current = source;

        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException(
                        "Cannot resolve path '" + path + "' at segment '" + segment + "'");
            }

            if (!map.containsKey(segment)) {
                throw new IllegalArgumentException(
                        "Path '" + path + "' does not exist");
            }

            current = map.get(segment);
        }

        return current;
    }
}
