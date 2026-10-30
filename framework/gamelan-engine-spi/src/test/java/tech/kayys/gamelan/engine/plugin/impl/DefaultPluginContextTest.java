package tech.kayys.gamelan.engine.plugin.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import tech.kayys.gamelan.engine.plugin.PluginMetadata;
import tech.kayys.gamelan.engine.plugin.PluginRuntimeInfo;
import tech.kayys.gamelan.engine.plugin.SemVer;

class DefaultPluginContextTest {

    @Test
    void runtimeInfoUsesExplicitRuntimeModeAndEngineVersion() {
        DefaultPluginContext context = new DefaultPluginContext(
                new PluginMetadata("test", "Test Plugin", "1.2.3", "author", "desc", null, null),
                LoggerFactory.getLogger(DefaultPluginContextTest.class),
                Map.of(
                        "gamelan.runtime.mode", "executor",
                        "gamelan.engine.version", "1.4.2"),
                null,
                null,
                null,
                null,
                "/tmp/plugin");

        PluginRuntimeInfo runtimeInfo = context.runtimeInfo();

        assertEquals("executor", runtimeInfo.runtimeMode());
        assertEquals(new SemVer(1, 4, 2), runtimeInfo.engineVersion());
    }

    @Test
    void runtimeInfoMapsDeploymentModeWhenRuntimeModeMissing() {
        DefaultPluginContext context = new DefaultPluginContext(
                new PluginMetadata("test", "Test Plugin", "1.2.3", "author", "desc", null, null),
                LoggerFactory.getLogger(DefaultPluginContextTest.class),
                Map.of(
                        "gamelan.deployment-mode", "enterprise",
                        "gamelan.engine.version", "2.0.1-SNAPSHOT"),
                null,
                null,
                null,
                null,
                "/tmp/plugin");

        PluginRuntimeInfo runtimeInfo = context.runtimeInfo();

        assertEquals("distributed", runtimeInfo.runtimeMode());
        assertEquals(new SemVer(2, 0, 1), runtimeInfo.engineVersion());
    }

    @Test
    void runtimeInfoFallsBackWhenConfigurationMissingOrInvalid() {
        DefaultPluginContext context = new DefaultPluginContext(
                new PluginMetadata("test", "Test Plugin", "1.2.3", "author", "desc", null, null),
                LoggerFactory.getLogger(DefaultPluginContextTest.class),
                Map.of("gamelan.engine.version", "not-a-semver"),
                null,
                null,
                null,
                null,
                "/tmp/plugin");

        PluginRuntimeInfo runtimeInfo = context.runtimeInfo();

        assertEquals("standalone", runtimeInfo.runtimeMode());
        assertEquals(new SemVer(0, 0, 0), runtimeInfo.engineVersion());
    }
    @Test
    void configurationSupportsTypedReadsAndScopedKeys() {
        DefaultPluginContext context = new DefaultPluginContext(
                new PluginMetadata("test", "Test Plugin", "1.2.3", "author", "desc", null, null),
                LoggerFactory.getLogger(DefaultPluginContextTest.class),
                Map.of(
                        "plugin.timeout", "30",
                        "plugin.enabled", "true",
                        "plugin.name", "example"),
                null,
                null,
                null,
                null,
                "/tmp/plugin");

        var pluginConfig = context.config().scoped("plugin");

        assertEquals(30, pluginConfig.get("timeout", Integer.class).orElseThrow());
        assertEquals(true, pluginConfig.get("enabled", Boolean.class).orElseThrow());
        assertEquals("example", pluginConfig.require("name"));
        assertEquals("30", context.config().get("plugin.timeout").orElseThrow());
        assertThrows(IllegalStateException.class, () -> pluginConfig.require("missing"));
    }

    @Test
    void configurationRejectsInvalidTypedValues() {
        DefaultPluginContext context = new DefaultPluginContext(
                new PluginMetadata("test", "Test Plugin", "1.2.3", "author", "desc", null, null),
                LoggerFactory.getLogger(DefaultPluginContextTest.class),
                Map.of("plugin.enabled", "sometimes"),
                null,
                null,
                null,
                null,
                "/tmp/plugin");

        assertThrows(
                IllegalArgumentException.class,
                () -> context.config().get("plugin.enabled", Boolean.class));
    }

}
