package tech.kayys.gamelan.engine.run;

import java.util.LinkedHashMap;
import java.util.Map;

import tech.kayys.gamelan.engine.error.ErrorCode;
import tech.kayys.gamelan.engine.error.GamelanException;

/**
 * Run labels and correlation id, carried in the run's variables under reserved keys so that every run repository already persists them.
 *
 * <p>Both used to be accepted by the SDK and then dropped. They are visible to workflow nodes as variables named {@value #LABELS} and
 * {@value #CORRELATION_ID}; {@link #userVariables(Map)} removes them for response {@code outputs}.
 */
public final class RunMetadata {

    public static final String LABELS = "_gamelan.labels";
    public static final String CORRELATION_ID = "_gamelan.correlationId";

    public static final int MAX_LABELS = 32;
    public static final int MAX_LENGTH = 128;

    private RunMetadata() {
    }

    /**
     * @return a copy of {@code inputs} with the labels and correlation id added
     * @throws GamelanException (VALIDATION_FAILED) when the labels or the correlation id are over their limits, or when the inputs already use a reserved key
     */
    public static Map<String, Object> attach(Map<String, Object> inputs, Map<String, String> labels, String correlationId) {
        Map<String, Object> out = new LinkedHashMap<>(inputs == null ? Map.of() : inputs);
        if (out.containsKey(LABELS) || out.containsKey(CORRELATION_ID)) {
            throw new GamelanException(ErrorCode.VALIDATION_FAILED, "inputs may not use the reserved keys " + LABELS + " and " + CORRELATION_ID);
        }
        if (labels != null && !labels.isEmpty()) {
            if (labels.size() > MAX_LABELS) {
                throw new GamelanException(ErrorCode.VALIDATION_FAILED, "at most " + MAX_LABELS + " labels");
            }
            Map<String, String> copy = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : labels.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (k == null || k.isBlank() || k.length() > MAX_LENGTH || v == null || v.length() > MAX_LENGTH) {
                    throw new GamelanException(ErrorCode.VALIDATION_FAILED, "a label needs a non-blank key and values of at most " + MAX_LENGTH + " characters");
                }
                copy.put(k, v);
            }
            out.put(LABELS, copy);
        }
        if (correlationId != null && !correlationId.isBlank()) {
            if (correlationId.length() > MAX_LENGTH) {
                throw new GamelanException(ErrorCode.VALIDATION_FAILED, "correlationId is longer than " + MAX_LENGTH + " characters");
            }
            out.put(CORRELATION_ID, correlationId);
        }
        return out;
    }

    /** The labels stored by {@link #attach}, or an empty map. */
    public static Map<String, String> labels(Map<String, Object> variables) {
        Map<String, String> out = new LinkedHashMap<>();
        if (variables != null && variables.get(LABELS) instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (e.getKey() instanceof String k && e.getValue() instanceof String v) {
                    out.put(k, v);
                }
            }
        }
        return out;
    }

    /** The correlation id stored by {@link #attach}, or null. */
    public static String correlationId(Map<String, Object> variables) {
        return variables != null && variables.get(CORRELATION_ID) instanceof String s ? s : null;
    }

    /** {@code variables} without the reserved keys. */
    public static Map<String, Object> userVariables(Map<String, Object> variables) {
        if (variables == null) {
            return Map.of();
        }
        Map<String, Object> out = new LinkedHashMap<>(variables);
        out.remove(LABELS);
        out.remove(CORRELATION_ID);
        return out;
    }
}
