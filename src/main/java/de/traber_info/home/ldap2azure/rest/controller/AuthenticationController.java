package de.traber_info.home.ldap2azure.rest.controller;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.rest.anotation.CheckAuth;
import de.traber_info.home.ldap2azure.rest.exception.NotFoundException;
import de.traber_info.home.ldap2azure.rest.model.object.ApiKey;
import de.traber_info.home.ldap2azure.rest.model.request.ApiKeyRequest;
import de.traber_info.home.ldap2azure.rest.model.response.LoginResponse;
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
     * Method used by the client to get a new session
     * @param password Password send by the client
     * @return Returns an {@link LoginResponse} in case the login is successful or an error if it fails.
     */
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response login(@FormDataParam("password") String password) {
        if (!AuthenticationService.validatePassword(password)) {
            LOG.warn("Login failed from IP {} using password {} Reason: Invalid password", sr.getRemoteAddr(), password);
            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":\"invalid_password\"}")
                    .build();
        }
        String sessionKey = AuthenticationService.issueSession();
        LoginResponse response = new LoginResponse(
                H2Helper.getSyncDao().getRecent(4),
                H2Helper.getUserDao().getOkAmount(),
                H2Helper.getUserDao().getPendingAmount(),
                H2Helper.getUserDao().getFailedAmount()
        );
        try {
            String cookieHostname = httpHeaders.getRequestHeader("host").get(0)
                    .substring(0, httpHeaders.getRequestHeader("host").get(0).lastIndexOf(':'));
            return Response
                    .ok()
                    .entity(response)
                    .cookie(new NewCookie(new Cookie("cdsess", sessionKey, "/", cookieHostname)))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method used by the client to invalidate an existing session.
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
    @Path("/api_key")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ApiKey createApiKey(@Valid ApiKeyRequest apiKeyRequest) {
        return AuthenticationService.createApiKey(apiKeyRequest.getKeyName());
    }

    /**
     * Get a list of {@link ApiKey} currently stored in the database.
     * @return List of {@link ApiKey} currently stored in the database.
     */
    @GET
    @CheckAuth
    @Path("/api_key")
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
    @Path("/api_key/{id}")
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
    @Path("/api_key/{key_id}/secret")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiKeySecret(@NotNull @PathParam("key_id") String keyId) {
        return Response
                .ok()
                .entity("{\"secret\":\"" + AuthenticationService.getApiKeySecret(keyId) + "\"}")
                .build();
    }

    /**
     * Delete the {@link ApiKey} with the given id from the database.
     * @param keyId Id of the {@link ApiKey} that should be deleted.
     */
    @DELETE
    @CheckAuth
    @Path("/api_key/{key_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteApiKey(@NotNull @PathParam("key_id") String keyId) {
        AuthenticationService.deleteApiKey(keyId);
        return Response
                .ok()
                .entity("[]")
                .build();
    }

}
