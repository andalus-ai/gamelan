package tech.kayys.gamelan.core.config;

import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.gamelan.engine.config.Configuration;

/**
 * MicroProfile Config-backed implementation of the engine configuration SPI.
 *
 * <p>The engine core depends only on the portable configuration contract;
 * this adapter supplies the MicroProfile Config integration for CDI runtimes.</p>
 */
@ApplicationScoped
public class MicroProfileConfiguration implements Configuration {

    private final Config config;
    private final String prefix;

    public MicroProfileConfiguration() {
        this(ConfigProvider.getConfig(), "");
    }

    private MicroProfileConfiguration(Config config, String prefix) {
        this.config = config;
        this.prefix = prefix;
    }

    @Override
    public Optional<String> get(String key) {
        return config.getOptionalValue(qualify(key), String.class);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return config.getOptionalValue(qualify(key), type);
    }

    @Override
    public String require(String key) {
        return get(key).orElseThrow(
                () -> new IllegalStateException("Missing required configuration: " + qualify(key)));
    }

    @Override
    public Configuration scoped(String scope) {
        String normalizedScope = scope == null ? "" : scope.trim().replaceAll("^\\.+|\\.+$", "");
        String scopedPrefix = normalizedScope.isEmpty()
                ? prefix
                : prefix.isEmpty() ? normalizedScope : prefix + "." + normalizedScope;
        return new MicroProfileConfiguration(config, scopedPrefix);
    }

    private String qualify(String key) {
        return prefix.isEmpty() ? key : prefix + "." + key;
    }
}
