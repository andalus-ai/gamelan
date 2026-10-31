package tech.kayys.gamelan.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.kayys.gamelan.engine.tenant.TenantId;

/**
 * Engine-level configuration for deployment and tenant behavior.
 *
 * <p>The runtime profiles configure these values with
 * {@code gamelan.multi-tenancy.enabled} and {@code gamelan.tenant.default-id}.
 * Defaults keep the engine usable in local/test environments while requiring
 * explicit tenant identity when multi-tenancy is enabled.</p>
 */
@ApplicationScoped
public class GamelanConfig {

    private final boolean multiTenancyEnabled;
    private final TenantId defaultTenant;

    @Inject
    public GamelanConfig(
            @ConfigProperty(name = "gamelan.multi-tenancy.enabled", defaultValue = "false")
            boolean multiTenancyEnabled,
            @ConfigProperty(name = "gamelan.tenant.default-id", defaultValue = "default-tenant")
            String defaultTenantId) {
        this.multiTenancyEnabled = multiTenancyEnabled;
        this.defaultTenant = TenantId.of(defaultTenantId);
    }

    public boolean isMultiTenancyEnabled() {
        return multiTenancyEnabled;
    }

    public TenantId getDefaultTenant() {
        return defaultTenant;
    }
}
