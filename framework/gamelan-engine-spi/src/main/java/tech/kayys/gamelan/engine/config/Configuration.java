package tech.kayys.gamelan.engine.config;

import java.util.Optional;

/**
 * Read-only configuration contract exposed to engine and plugin integrations.
 *
 * <p>Implementations may provide scoped views so components can consume
 * configuration without depending on a runtime-specific configuration system.</p>
 */
public interface Configuration {

    /**
     * Returns the string value associated with the key, if present.
     */
    Optional<String> get(String key);

    /**
     * Returns the value converted to the requested type, if present.
     *
     * @throws IllegalArgumentException if the configured value cannot be converted
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Returns the required string value.
     *
     * @throws IllegalStateException if the key is not configured
     */
    String require(String key);

    /**
     * Returns a view whose keys are relative to the supplied prefix.
     */
    Configuration scoped(String prefix);
}
