package tech.kayys.gamelan.sdk.executor.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import tech.kayys.gamelan.engine.execution.ExecutionToken;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.protocol.CommunicationType;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;

class BaseExecutorRuntimeTest {

    @Test
    void sendResultEnrichesResultWithTenantFromTaskContext() {
        RecordingRuntime runtime = new RecordingRuntime();
        NodeExecutionTask task = task(Map.of(NodeExecutionTask.TENANT_ID_KEY, "tenant-a"));
        NodeExecutionResult result = SimpleNodeExecutionResult.success(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                Map.of("ok", true),
                task.token(),
                Duration.ZERO);

        runtime.send(task, result);

        assertEquals("tenant-a", runtime.recordingTransport.lastResult.getMetadata().get(NodeExecutionTask.TENANT_ID_KEY));
        assertEquals("tenant-a", runtime.recordingTransport.lastResult.getMetadata().get("tenantId"));
    }

    @Test
    void selectExecutorPrefersExactExecutorTypeFromTaskContext() {
        RecordingRuntime runtime = new RecordingRuntime();
        TestExecutor genericExecutor = new TestExecutor("generic", true);
        TestExecutor agentExecutor = new TestExecutor("agent.planner", true);
        runtime.registerExecutor(genericExecutor);
        runtime.registerExecutor(agentExecutor);

        WorkflowExecutor selected = runtime.select(task(Map.of(NodeExecutionTask.NODE_TYPE_KEY, "agent.planner")));

        assertSame(agentExecutor, selected);
    }

    @Test
    void selectExecutorFallsBackToReadyCompatibleExecutor() {
        RecordingRuntime runtime = new RecordingRuntime();
        TestExecutor unavailableExecutor = new TestExecutor("unavailable", false);
        TestExecutor readyExecutor = new TestExecutor("ready", true);
        runtime.registerExecutor(unavailableExecutor);
        runtime.registerExecutor(readyExecutor);

        WorkflowExecutor selected = runtime.select(task(Map.of(NodeExecutionTask.NODE_TYPE_KEY, "missing")));

        assertSame(readyExecutor, selected);
    }

    @Test
    void executorTypesListParsesAndBlankMeansAll() {
        assertEquals(null, BaseExecutorRuntime.parseExecutorTypes(null));
        assertEquals(null, BaseExecutorRuntime.parseExecutorTypes("  "));
        assertEquals(null, BaseExecutorRuntime.parseExecutorTypes(" , ,"));
        assertEquals(java.util.Set.of("a", "b-c"), BaseExecutorRuntime.parseExecutorTypes(" a , b-c ,,"));
    }

    @Test
    void discoveryServesOnlyTheConfiguredExecutorTypes() {
        TestExecutor service = new TestExecutor("wayang-service", true);
        TestExecutor agent = new TestExecutor("wayang-agent", true);

        RecordingRuntime all = new RecordingRuntime(java.util.List.of(service, agent), null);
        all.initialize();
        assertEquals(java.util.Set.of("wayang-service", "wayang-agent"), all.getExecutors().keySet(), "not configured: everything, as before");

        RecordingRuntime onlyService = new RecordingRuntime(java.util.List.of(service, agent), java.util.Set.of("wayang-service", "nope"));
        onlyService.initialize();
        assertEquals(java.util.Set.of("wayang-service"), onlyService.getExecutors().keySet());

        RecordingRuntime manual = new RecordingRuntime(java.util.List.of(service), java.util.Set.of("wayang-agent"));
        manual.registerExecutor(new TestExecutor("hand-made", true));
        manual.initialize();
        assertEquals(java.util.Set.of("hand-made"), manual.getExecutors().keySet(), "registered by hand is never filtered; discovery honours the list");
    }

    private static NodeExecutionTask task(Map<String, Object> context) {
        WorkflowRunId runId = WorkflowRunId.of("run-1");
        NodeId nodeId = NodeId.of("node-1");
        return new NodeExecutionTask(
                runId,
                nodeId,
                1,
                new ExecutionToken("token-1", runId, nodeId, 1, java.time.Instant.now().plusSeconds(60)),
                context,
                RetryPolicy.none());
    }

    private static final class RecordingRuntime extends BaseExecutorRuntime {
        private final RecordingTransport recordingTransport = new RecordingTransport();

        private final java.util.Set<String> serve;

        private RecordingRuntime() {
            this(java.util.List.of(), null);
        }

        /** @param found what CDI would offer; @param serve the configured executor types, or null for all */
        @SuppressWarnings("unchecked")
        private RecordingRuntime(java.util.List<WorkflowExecutor> found, java.util.Set<String> serve) {
            this.transport = recordingTransport;
            this.serve = serve;
            // Instance has many abstract methods; only iteration is used by discovery
            this.discoveredExecutors = (jakarta.enterprise.inject.Instance<WorkflowExecutor>) java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(), new Class<?>[] { jakarta.enterprise.inject.Instance.class }, (proxy, method, args) -> {
                        if (method.getName().equals("iterator")) {
                            return found.iterator();
                        }
                        if (method.getName().equals("forEach")) {
                            found.forEach((java.util.function.Consumer<WorkflowExecutor>) args[0]);
                            return null;
                        }
                        throw new UnsupportedOperationException(method.getName());
                    });
        }

        @Override
        protected java.util.Set<String> allowedExecutorTypes() {
            return serve;
        }

        @Override
        protected ExecutorTransport createTransport() {
            return recordingTransport;
        }

        private void send(NodeExecutionTask task, NodeExecutionResult result) {
            sendResult(task, result);
        }

        private WorkflowExecutor select(NodeExecutionTask task) {
            return selectExecutor(task);
        }
    }

    private static final class RecordingTransport implements ExecutorTransport {
        private NodeExecutionResult lastResult;

        @Override
        public CommunicationType getCommunicationType() {
            return CommunicationType.LOCAL;
        }

        @Override
        public Multi<NodeExecutionTask> receiveTasks() {
            return Multi.createFrom().empty();
        }

        @Override
        public Uni<Void> sendResult(NodeExecutionResult result) {
            lastResult = result;
            return Uni.createFrom().voidItem();
        }
    }

    private record TestExecutor(String executorType, boolean ready) implements WorkflowExecutor {
        @Override
        public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
            return Uni.createFrom().failure(new UnsupportedOperationException("not used"));
        }

        @Override
        public String getExecutorType() {
            return executorType;
        }

        @Override
        public boolean isReady() {
            return ready;
        }
    }
}
