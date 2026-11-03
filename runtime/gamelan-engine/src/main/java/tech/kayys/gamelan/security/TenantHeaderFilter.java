package tech.kayys.gamelan.security;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import tech.kayys.gamelan.engine.context.RequestContext;

/**
 * Extracts correlation metadata from request headers.
 *
 * <p>{@code X-Tenant-ID} is deliberately not trusted here. Tenant identity
 * must be established by the authenticated identity (for example, the
 * verified JWT tenant claim) or by an explicitly authenticated internal
 * integration. A caller-controlled routing header must never select the
 * tenant whose workflow data is read or mutated.</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 2)
public class TenantHeaderFilter implements ContainerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Inject
    RequestContext requestContext;

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String requestId = ctx.getHeaderString(REQUEST_ID_HEADER);
        if (requestId != null && !requestId.isBlank()) {
            requestContext.setRequestId(requestId);
        }
    }
}
