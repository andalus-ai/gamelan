package tech.kayys.gamelan.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.container.ContainerRequestContext;
import tech.kayys.gamelan.engine.context.RequestContext;
import tech.kayys.gamelan.engine.tenant.TenantId;

class TenantHeaderFilterTest {

    @Test
    void callerSuppliedTenantHeaderDoesNotEstablishTenantContext() throws IOException {
        RequestContext requestContext = new RequestContext();
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString("X-Tenant-ID")).thenReturn("attacker-tenant");
        when(request.getHeaderString("X-Request-ID")).thenReturn("request-123");

        TenantHeaderFilter filter = new TenantHeaderFilter();
        filter.requestContext = requestContext;
        filter.filter(request);

        assertFalse(requestContext.hasTenant(),
                "an untrusted request header must not establish tenant identity");
        assertEquals("request-123", requestContext.getRequestId());
    }

    @Test
    void callerSuppliedTenantHeaderCannotOverrideJwtTenantContext() throws IOException {
        RequestContext requestContext = new RequestContext();
        requestContext.setTenantId(TenantId.of("jwt-tenant"));

        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString("X-Tenant-ID")).thenReturn("attacker-tenant");

        TenantHeaderFilter filter = new TenantHeaderFilter();
        filter.requestContext = requestContext;
        filter.filter(request);

        assertTrue(requestContext.hasTenant());
        assertEquals(TenantId.of("jwt-tenant"), requestContext.getTenantId().orElseThrow());
    }

    @Test
    void blankTenantHeaderHasNoEffect() throws IOException {
        RequestContext requestContext = new RequestContext();
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        when(request.getHeaderString("X-Tenant-ID")).thenReturn("  ");

        TenantHeaderFilter filter = new TenantHeaderFilter();
        filter.requestContext = requestContext;
        filter.filter(request);

        assertFalse(requestContext.hasTenant());
    }
}
