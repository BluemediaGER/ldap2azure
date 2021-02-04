package de.traber_info.home.ldap2azure.rest.filter;

import de.traber_info.home.ldap2azure.rest.anotation.CheckAuth;
import de.traber_info.home.ldap2azure.rest.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;

/**
 * Request filter that checks if the user is authenticated,
 * when an REST method is annotated with the {@link CheckAuth} annotation.
 *
 * @author Oliver Traber
 */
@Provider
@CheckAuth
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class.getName());

    /** {@link HttpServletRequest} used to log the senders ip address if an session validation fails */
    @Context
    private HttpServletRequest sr;

    /**
     * Filter method called by the Jersey Servlet Container when an matching request arrives.
     * @param context {@link ContainerRequestContext} used to access client cookies.
     */
    @Override
    public void filter(ContainerRequestContext context) {
        Cookie sessCookie = null;

        // Check if the client has sent an bearer authentication token and validate it
        if (context.getHeaderString("X-API-Key") != null) {
            if (AuthenticationService.validateApiKey(context.getHeaderString("X-API-Key"))) {
                return;
            } else {
                abort(context, "Api key invalid");
            }
        }

        if (context.getCookies().containsKey("cdsess")) {
            sessCookie = context.getCookies().get("cdsess");
        }

        if (sessCookie == null) {
            abort(context, "Session cookie not set");
            return;
        }

        if (!AuthenticationService.validateSession(sessCookie.getValue())) {
            abort(context, "Session cookie invalid or expired");
        }

    }

    /**
     * Method called when the session cookie sent by the client is either null or invalid.
     * Rejects the request and sends the corresponding error to the client.
     * @param context {@link ContainerRequestContext} used to abort the request.
     */
    public void abort(ContainerRequestContext context, String reason) {
        LOG.warn("Rejected request from ip address {} Reason: {}", sr.getRemoteAddr(), reason);
        context.abortWith(
                Response
                        .status(Response.Status.UNAUTHORIZED)
                        .cookie(new NewCookie(new Cookie("cdsess", "", "/", context.getHeaderString("host").substring(0, context.getHeaderString("host").lastIndexOf(':')))))
                        .entity("{\"error\":\"not_authenticated\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

}
