package de.traber_info.home.ldap2azure.rest.controller;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.rest.anotation.CheckAuth;
import de.traber_info.home.ldap2azure.rest.anotation.CheckPermission;
import de.traber_info.home.ldap2azure.rest.exception.BadRequestException;
import de.traber_info.home.ldap2azure.rest.exception.NotFoundException;
import de.traber_info.home.ldap2azure.rest.model.object.ApiKey;
import de.traber_info.home.ldap2azure.rest.model.object.ApiUser;
import de.traber_info.home.ldap2azure.rest.model.request.ApiKeyCreateRequest;
import de.traber_info.home.ldap2azure.rest.model.request.ApiUserCreateRequest;
import de.traber_info.home.ldap2azure.rest.model.request.PasswordUpdateRequest;
import de.traber_info.home.ldap2azure.rest.model.request.PermissionUpdateRequest;
import de.traber_info.home.ldap2azure.rest.model.response.DashboardResponse;
import de.traber_info.home.ldap2azure.rest.model.types.Permission;
import de.traber_info.home.ldap2azure.rest.service.AuthenticationService;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * REST controller used to handle all authentication actions.
 *
 * @author Oliver Traber
 */
@Path("/auth")
public class AuthenticationController {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationController.class.getName());

    /** Injected {@link HttpHeaders} used to get the hostname used to set cookies on the client */
    @Context
    private HttpHeaders httpHeaders;

    /** Injected {@link HttpServletRequest} used to log the clients ip address in case the login fails */
    @Context
    private HttpServletRequest sr;

    /**
     * Method used by an frontend to obtain a new session.
     * @param password Password send by the client.
     * @return Returns an {@link DashboardResponse} in case the login is successful, or an error if it fails.
     */
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response login(@FormDataParam("username") String username, @FormDataParam("password") String password) {
        if (!AuthenticationService.validateCredentials(username, password)) {
            LOG.warn("Login failed from IP {} using username {} Reason: Invalid credentials", sr.getRemoteAddr(), username);
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"invalid_credentials\"}")
                    .build();
        }
        String sessionKey = AuthenticationService.issueSession(username);
        try {
            String cookieHostname = httpHeaders.getRequestHeader("host").get(0)
                    .substring(0, httpHeaders.getRequestHeader("host").get(0).lastIndexOf(':'));
            return Response
                    .ok()
                    .entity(H2Helper.getApiUserDao().getByAttributeMatch("username", username))
                    .cookie(new NewCookie(new Cookie("cdsess", sessionKey, "/", cookieHostname)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method used by an frontend to invalidate an existing session.
     * @param sessionCookie Session cookie sent by the client.
     * @return Returns an empty array and http status 200 in all cases. Sets an empty session cookie on the client.
     */
    @GET
    @CheckAuth
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@CookieParam("cdsess") Cookie sessionCookie) {
        if (sessionCookie != null) AuthenticationService.invalidateSession(sessionCookie.getValue());
        return Response
                .ok()
                .entity("[]")
                .cookie(new NewCookie(new Cookie("cdsess", "", "/", httpHeaders.getRequestHeader("host").get(0).substring(0, httpHeaders.getRequestHeader("host").get(0).lastIndexOf(':')))))
                .build();
    }

    /**
     * Method used by the client to keep an session valid, even if the user is not performing any actions.
     * @return Empty array an http status 200.
     */
    @GET
    @CheckAuth
    @Path("/ack")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ack() {
        return Response
                .ok()
                .entity("[]")
                .build();
    }

    /**
     * Create an new {@link ApiKey} and store it in the database.
     * @return Created {@link ApiKey}.
     */
    @POST
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("/api-key")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createApiKey(@Valid ApiKeyCreateRequest apiKeyCreateRequest) {
        ApiKey key = AuthenticationService.createApiKey(apiKeyCreateRequest);
        return Response.created(null).entity(key).build();
    }

    /**
     * Get a list of {@link ApiKey} currently stored in the database.
     * @return List of {@link ApiKey} currently stored in the database.
     */
    @GET
    @CheckAuth
    @Path("/api-key")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiKey> getApiKeys() {
        return H2Helper.getApiKeyDao().getAll();
    }

    /**
     * Get a single {@link ApiKey} by supplying it's id.
     * @param apiKeyId Id of the {@link ApiKey} you want to get.
     * @return The {@link ApiKey} object that matches the given id.
     */
    @GET
    @CheckAuth
    @Path("/api-key/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiKey getApiKey(@NotEmpty @PathParam("id") String apiKeyId) {
        ApiKey apiKey = H2Helper.getApiKeyDao().getByAttributeMatch("id", apiKeyId);
        if (apiKey == null) throw new NotFoundException("apikey_not_existing");
        return apiKey;
    }

    /**
     * Get the secret from the {@link ApiKey} using the given id.
     * @param keyId Id of the {@link ApiKey} the secret should be got from.
     * @return Secret corresponding to the given {@link ApiKey}.
     */
    @GET
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("/api-key/{key_id}/secret")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiKeySecret(@NotNull @PathParam("key_id") String keyId) {
        return Response
                .ok()
                .entity("{\"secret\":\"" + AuthenticationService.getApiKeySecret(keyId) + "\"}")
                .build();
    }

    /**
     * Update the {@link Permission} of an existing {@link ApiKey}.
     * @param authorizationHeader Authorization header used to prevent an {@link ApiKey}
     *                            from changing it's own permission.
     * @param keyId Id of the {@link ApiKey} the {@link Permission} should be updated for.
     * @param permissionUpdateRequest {@link PermissionUpdateRequest} containing the new {@link Permission}.
     * @return Updated {@link ApiKey} if the operation was successful.
     */
    @PUT
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/api-key/{id}/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiKey updateApiKeyPermission(@HeaderParam("Authorization") String authorizationHeader,
                                         @PathParam("id") String keyId,
                                         @Valid PermissionUpdateRequest permissionUpdateRequest) {
        // Prevent api key from changing it's own permission
        if(authorizationHeader != null) {
            ApiKey requestingKey = AuthenticationService.getApiKey(authorizationHeader);
            if (keyId.equals(requestingKey.getId())) {
                throw new BadRequestException("cant_change_own_permission");
            }
        }
        ApiKey key = H2Helper.getApiKeyDao().getByAttributeMatch("id", keyId);
        if (key == null) throw new NotFoundException("apikey_not_existing");
        key.updatePermission(permissionUpdateRequest.getPermission());
        H2Helper.getApiKeyDao().update(key);
        return key;
    }

    /**
     * Delete the {@link ApiKey} with the given id from the database.
     * @param keyId Id of the {@link ApiKey} that should be deleted.
     */
    @DELETE
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("/api-key/{key_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApiKey(@NotNull @PathParam("key_id") String keyId) {
        AuthenticationService.deleteApiKey(keyId);
        return Response
                .ok()
                .entity("[]")
                .build();
    }

    /**
     * Create an new {@link ApiUser} in the database.
     * @param request {@link ApiUserCreateRequest} containing the details for the new user.
     * @return Created {@link ApiUser} if the operation was successful.
     */
    @POST
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("/api-user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createApiUser(@Valid ApiUserCreateRequest request) {
        ApiUser user = AuthenticationService.createApiUser(request);
        return Response.created(null).entity(user).build();
    }

    /**
     * Get the list of all {@link ApiUser} currently stored in the database.
     * @return {@link List} containing all {@link ApiUser} currently stored in the database.
     */
    @GET
    @CheckAuth
    @Path("/api-user")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ApiUser> getApiUsers() {
        return H2Helper.getApiUserDao().getAll();
    }

    /**
     * Get an specific {@link ApiUser} by it's id.
     * @param userId Id of the {@link ApiUser} that should be retrieved.
     * @return {@link ApiUser} matching the given id.
     */
    @GET
    @CheckAuth
    @Path("/api-user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiUser getApiUser(@PathParam("id") String userId) {
        ApiUser user = H2Helper.getApiUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_found");
        return user;
    }

    /**
     * Update the password of the currently logged in {@link ApiUser}.
     * @param sessCookie Session key of the currently logged in {@link ApiUser}.
     * @param passwordUpdateRequest {@link PasswordUpdateRequest} containing the new password for the user.
     * @return Updated {@link ApiUser} if the operation was successful.
     */
    @PUT
    @CheckAuth
    @Path("/api-user/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApiUser updateApiUserPassword(@CookieParam("cdsess") Cookie sessCookie,
                                         @Valid PasswordUpdateRequest passwordUpdateRequest) {
        if (sessCookie == null) throw new BadRequestException("only_user_sessions_allowed");
        return AuthenticationService.updateApiUserPassword(sessCookie.getValue(), passwordUpdateRequest.getPassword());
    }

    /**
     * Update the password of an {@link ApiUser} based on it's id.
     * @param userId Id of the {@link ApiUser} that should be updated.
     * @param passwordUpdateRequest {@link PasswordUpdateRequest} containing the new password for the user.
     * @return Updated {@link ApiUser} if the operation was successful.
     */
    @PUT
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("/api-user/{id}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApiUser updateApiUserPassword(@PathParam("id") String userId,
                                         @Valid PasswordUpdateRequest passwordUpdateRequest) {
        return AuthenticationService.updateApiUserPasswordById(userId, passwordUpdateRequest.getPassword());
    }

    /**
     * Update the {@link Permission} for an {@link ApiUser} based on it's id.
     * @param sessCookie Session key to prevent an user from changing his own permission.
     * @param userId Id of the {@link ApiUser} that should be updated.
     * @param permissionUpdateRequest {@link PermissionUpdateRequest} containing the new {@link Permission}.
     * @return Updated {@link ApiUser} if the operation was successful.
     */
    @PUT
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/api-user/{id}/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiUser updateApiUserPermission(@CookieParam("cdsess") Cookie sessCookie,
                                           @PathParam("id") String userId,
                                           @Valid PermissionUpdateRequest permissionUpdateRequest) {
        // Prevent user from changing his own permission
        if (sessCookie != null) {
            ApiUser sessUser = AuthenticationService.getApiUserBySession(sessCookie.getValue());
            if (sessUser != null) {
                if (userId.equals(sessUser.getId())) {
                    throw new BadRequestException("cant_change_own_permission");
                }
            }
        }
        ApiUser user = H2Helper.getApiUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        user.updatePermission(permissionUpdateRequest.getPermission());
        H2Helper.getApiUserDao().update(user);
        return user;
    }

    /**
     * Delete an {@link ApiUser} from the database.
     * @param sessCookie Session key to prevent an user from deleting himself.
     * @param userId Id of the {@link ApiUser} that should be deleted.
     * @return Empty array and status 200 if the operation was successful.
     */
    @DELETE
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("/api-user/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApiUser(@CookieParam("cdsess") Cookie sessCookie, @PathParam("id") String userId) {
        return AuthenticationService.deleteApiUser(sessCookie, userId);
    }

}
