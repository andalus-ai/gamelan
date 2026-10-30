package tech.kayys.gamelan.core.config;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Runtime configuration for loading external Gamelan plugins.
 *
 * <p>Supported properties use the {@code gamelan.plugins} prefix:
 * {@code enabled}, {@code directories}, {@code disabled}, and
 * {@code fail-on-error}. Missing properties are left optional so the engine
 * can apply its documented defaults.</p>
 */
@ApplicationScoped
public class PluginConfig {

    private final Optional<Boolean> enabled;
    private final Optional<List<String>> directories;
    private final Optional<List<String>> disabled;
    private final Optional<Boolean> failOnError;

    @Inject
    public PluginConfig(
            @ConfigProperty(name = "gamelan.plugins.enabled") Optional<Boolean> enabled,
            @ConfigProperty(name = "gamelan.plugins.directories") Optional<List<String>> directories,
            @ConfigProperty(name = "gamelan.plugins.disabled") Optional<List<String>> disabled,
            @ConfigProperty(name = "gamelan.plugins.fail-on-error") Optional<Boolean> failOnError) {
        this.enabled = enabled;
        this.directories = directories;
        this.disabled = disabled;
        this.failOnError = failOnError;
    }

    public Optional<Boolean> enabled() {
        return enabled;
    }

    public Optional<List<String>> directories() {
        return directories.map(List::copyOf);
    }

    public Optional<List<String>> disabled() {
        return disabled.map(List::copyOf);
    }

    public Optional<Boolean> failOnError() {
        return failOnError;
    }
}
