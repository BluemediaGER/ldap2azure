package de.traber_info.home.ldap2azure.rest.controller;

import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.model.type.SyncState;
import de.traber_info.home.ldap2azure.rest.anotation.CheckAuth;
import de.traber_info.home.ldap2azure.rest.anotation.CheckPermission;
import de.traber_info.home.ldap2azure.rest.exception.GenericException;
import de.traber_info.home.ldap2azure.rest.exception.NotFoundException;
import de.traber_info.home.ldap2azure.rest.model.request.ConflictResolveRequest;
import de.traber_info.home.ldap2azure.rest.model.types.Permission;
import de.traber_info.home.ldap2azure.rest.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

/**
 * REST controller used to handle all actions around the {@link User} objects.
 *
 * @author Oliver Traber
 */
@Path("/user")
public class UserController {

    /**
     * Get the full list of users currently stored in the database.
     * The results is paged so one page only contains 20 entities at a time.
     * @param page Page index you want to get. Defaults to 0 if the query parameter is not given.
     * @return Array containing 20 {@link User} objects from the given page from the database.
     */
    @GET
    @CheckAuth
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getUsers(@QueryParam("page") long page) {
        QueryBuilder<User, String> queryBuilder = H2Helper.getUserDao().getQueryBuilder();
        try {
            return queryBuilder.orderBy("lastChanged", false).limit(20L).offset(page*20L).query();
        } catch (SQLException ex) {
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR, "internal_error", ex.getMessage());
        }
    }

    /**
     * Get a single {@link User} by supplying it's id.
     * @param userId Id of the {@link User} you want to get.
     * @return The {@link User} object that matches the given id.
     */
    @GET
    @CheckAuth
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@NotEmpty @PathParam("id") String userId) {
        User user = H2Helper.getUserDao().getByAttributeMatch("id", userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        return user;
    }

    /**
     * Get the full list of users that have the OK sync status.
     * The results is paged so one page only contains 20 entities at a time.
     * @param page Page index you want to get. Defaults to 0 if the query parameter is not given.
     * @return Array containing 20 {@link User} objects from the given page from the database.
     */
    @GET
    @CheckAuth
    @Path("/ok")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getOkUsers(@QueryParam("page") long page) {
        QueryBuilder<User, String> queryBuilder = H2Helper.getUserDao().getQueryBuilder();
        try {
            return queryBuilder.limit(20L).offset(page*20L)
                    .where().eq("syncState", SyncState.OK).query();
        } catch (SQLException ex) {
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR, "internal_error", ex.getMessage());
        }
    }

    /**
     * Get the full list users that have the PENDING sync status.
     * The results is paged so one page only contains 20 entities at a time.
     * @param page Page index you want to get. Defaults to 0 if the query parameter is not given.
     * @return Array containing 20 {@link User} objects from the given page from the database.
     */
    @GET
    @CheckAuth
    @Path("/pending")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getPendingUsers(@QueryParam("page") long page) {
        QueryBuilder<User, String> queryBuilder = H2Helper.getUserDao().getQueryBuilder();
        try {
            return queryBuilder.limit(20L).offset(page*20L)
                    .where().eq("syncState", SyncState.PENDING).query();
        } catch (SQLException ex) {
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR, "internal_error", ex.getMessage());
        }
    }

    /**
     * Get the full list of users that have the FAILED sync status.
     * The results is paged so one page only contains 20 entities at a time.
     * @param page Page index you want to get. Defaults to 0 if the query parameter is not given.
     * @return Array containing 20 {@link User} objects from the given page from the database.
     */
    @GET
    @CheckAuth
    @Path("/failed")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getFailedUsers(@QueryParam("page") long page) {
        QueryBuilder<User, String> queryBuilder = H2Helper.getUserDao().getQueryBuilder();
        try {
            return queryBuilder.limit(20L).offset(page*20L)
                    .where().eq("syncState", SyncState.FAILED).query();
        } catch (SQLException ex) {
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR, "internal_error", ex.getMessage());
        }
    }

    /**
     * Retry the sync of an given, failed user.
     * @param userId Id of the {@link User} the resync should be tried for.
     * @return {@link User} object if the sync was successful, or an error containing more details if the retry failed.
     */
    @POST
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("/{id}/retry")
    @Produces(MediaType.APPLICATION_JSON)
    public User retrySync(@NotEmpty @PathParam("id") String userId) {
        return UserService.retrySync(userId);
    }

    /**
     * Get the amount of all, ok, failed and pending users.
     * @return Json containing the amount of users in each state.
     */
    @GET
    @CheckAuth
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserStatus() {
        long all = H2Helper.getUserDao().getAmount();
        long ok = H2Helper.getUserDao().getOkAmount();
        long failed = H2Helper.getUserDao().getFailedAmount();
        long pending = H2Helper.getUserDao().getPendingAmount();
        String responseJson = "{\"userCount\": " + all + ",\"usersOk\": " + ok
                + ",\"usersFailed\": " + failed + ",\"usersPending\": " + pending + "}";
        return Response
                .ok()
                .entity(responseJson)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Get possible conflicts in the Azure AD for the given user id. Only works if an user is marked as failed.
     * @param userId Id of the user the conflicts should be got for.
     * @return {@link List<User>} containing possible conflicting user objects.
     */
    @GET
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("{id}/conflicts")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getPotentialConflicts(@NotEmpty @PathParam("id") String userId) {
        return UserService.getPotentialConflicts(userId);
    }

    /**
     * Resolve an user conflict by merging or recreating user in Azure AD.
     * @param internalUserId Internal id of the failed user whose conflict should be resolved.
     * @param request {@link ConflictResolveRequest} containing details about how the conflict should be resolved.
     * @return Updated {@link User} if the resolution of the conflict was successful.
     */
    @POST
    @CheckAuth
    @CheckPermission(Permission.READ_WRITE)
    @Path("{id}/conflicts/resolve")
    @Produces(MediaType.APPLICATION_JSON)
    public User resolveConflict(@NotEmpty @PathParam("id") String internalUserId,
                                @Valid ConflictResolveRequest request) {
        return UserService.resolveConflict(internalUserId, request.getAzureImmutableId(), request.getStrategy());
    }

}
