package de.traber_info.home.ldap2azure.rest.filter;

import de.traber_info.home.ldap2azure.rest.anotation.CheckPermission;
import de.traber_info.home.ldap2azure.rest.model.object.ApiKey;
import de.traber_info.home.ldap2azure.rest.model.object.ApiUser;
import de.traber_info.home.ldap2azure.rest.model.types.Permission;
import de.traber_info.home.ldap2azure.rest.service.AuthenticationService;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Request filter that checks if the user has the permission to perform to execute an REST method,
 * when the REST method is annotated with the {@link CheckPermission} annotation.
 *
 * @author Oliver Traber
 */
@CheckPermission
@Provider
@Priority(Priorities.AUTHORIZATION)
public class PermissionFilter implements ContainerRequestFilter {

    /** {@link ResourceInfo} used to access the annotated method. */
    @Context
    private ResourceInfo resourceInfo;

    /**
     * Filter method called by the Jersey Servlet Container when an matching request arrives.
     * @param context {@link ContainerRequestContext} used to access client cookies and the authorization header.
     */
    @Override
    public void filter(ContainerRequestContext context) {
        Cookie sessCookie = null;
        String authHeader = null;

        if (context.getCookies().containsKey("cdsess")) {
            sessCookie = context.getCookies().get("cdsess");
        }

        // Check if the client has sent an bearer authentication token
        if (context.getHeaderString("X-API-Key") != null) {
            authHeader = context.getHeaderString("X-API-Key");
        }

        Method method = resourceInfo.getResourceMethod();

        if (method != null) {
            CheckPermission annotation = method.getAnnotation(CheckPermission.class);
            List<Permission> allowedPermissions = Arrays.asList(annotation.value());

            if (sessCookie != null) {
                ApiUser user = AuthenticationService.getApiUserBySession(sessCookie.getValue());
                if (allowedPermissions.contains(user.getPermission())) return;
                abort(context);
            }
            if (authHeader != null) {
                ApiKey key = AuthenticationService.getApiKey(authHeader);
                if (allowedPermissions.contains(key.getPermission())) return;
                abort(context);
            }
        }

    }

    /**
     * Method called when the client is not allowed to call an method.
     * Rejects the request and sends the corresponding error to the client.
     * @param context {@link ContainerRequestContext} used to abort the request.
     */
    private void abort(ContainerRequestContext context) {
        context.abortWith(
                Response
                        .status(Response.Status.FORBIDDEN)
                        .entity("{\"error\":\"insufficient_permissions\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }

}
