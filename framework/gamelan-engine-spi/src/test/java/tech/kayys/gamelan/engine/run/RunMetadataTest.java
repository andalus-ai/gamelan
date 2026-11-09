package tech.kayys.gamelan.engine.run;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tech.kayys.gamelan.engine.error.GamelanException;

class RunMetadataTest {

    @Test
    void labelsAndCorrelationIdSurviveARoundTripAndLeaveInputsUntouched() {
        Map<String, Object> inputs = Map.of("orderId", "A1");
        Map<String, Object> vars = RunMetadata.attach(inputs, Map.of("team", "billing"), "corr-1");
        assertEquals(Map.of("team", "billing"), RunMetadata.labels(vars));
        assertEquals("corr-1", RunMetadata.correlationId(vars));
        assertEquals(inputs, RunMetadata.userVariables(vars), "reserved keys never show up as outputs");
        assertEquals(Map.of("orderId", "A1"), inputs, "the caller's map is not modified");
    }

    @Test
    void nothingIsAddedWhenThereIsNothingToAdd() {
        assertEquals(Map.of("x", 1), RunMetadata.attach(Map.of("x", 1), Map.of(), " "));
        assertEquals(Map.of(), RunMetadata.attach(null, null, null));
        assertEquals(Map.of(), RunMetadata.labels(null));
        assertNull(RunMetadata.correlationId(Map.of()));
    }

    @Test
    void limitsAndReservedKeysAreRefusedAsValidationErrors() {
        Map<String, String> many = new HashMap<>();
        for (int i = 0; i <= RunMetadata.MAX_LABELS; i++) {
            many.put("k" + i, "v");
        }
        assertThrows(GamelanException.class, () -> RunMetadata.attach(Map.of(), many, null));
        assertThrows(GamelanException.class, () -> RunMetadata.attach(Map.of(), Map.of("k", "x".repeat(129)), null));
        assertThrows(GamelanException.class, () -> RunMetadata.attach(Map.of(), Map.of(" ", "v"), null));
        assertThrows(GamelanException.class, () -> RunMetadata.attach(Map.of(), null, "c".repeat(129)));
        assertThrows(GamelanException.class, () -> RunMetadata.attach(Map.of(RunMetadata.LABELS, "x"), Map.of(), null),
                "a caller cannot forge the reserved keys through inputs");
    }

    @Test
    void malformedStoredValuesAreIgnoredNotTrusted() {
        Map<String, Object> vars = Map.of(RunMetadata.LABELS, Map.of("ok", "v", "bad", 5), RunMetadata.CORRELATION_ID, 7);
        assertEquals(Map.of("ok", "v"), RunMetadata.labels(vars));
        assertNull(RunMetadata.correlationId(vars));
    }

    @Test
    void theResponseCarriesLabelsAndCorrelationIdAndKeepsOutputsClean() {
        RunResponse r = RunResponse.builder().runId("r").labels(Map.of("a", "b")).correlationId("c").outputs(Map.of("o", 1)).build();
        assertEquals(Map.of("a", "b"), r.getLabels());
        assertEquals("c", r.getCorrelationId());
        assertFalse(r.getOutputs().containsKey(RunMetadata.LABELS));
        assertTrue(RunResponse.builder().build().getLabels().isEmpty());
        CreateRunRequest req = CreateRunRequest.builder().workflowId("w").labels(Map.of("a", "b")).build();
        assertEquals(Map.of("a", "b"), req.getLabels());
    }
}
