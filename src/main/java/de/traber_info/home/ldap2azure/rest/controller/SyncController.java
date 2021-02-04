package de.traber_info.home.ldap2azure.rest.controller;

import com.j256.ormlite.stmt.QueryBuilder;
import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.model.object.Sync;
import de.traber_info.home.ldap2azure.model.object.User;
import de.traber_info.home.ldap2azure.rest.anotation.CheckAuth;
import de.traber_info.home.ldap2azure.rest.exception.GenericException;
import de.traber_info.home.ldap2azure.rest.exception.NotFoundException;

import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

/**
 * REST controller used to handle all actions around the {@link Sync} objects.
 *
 * @author Oliver Traber
 */
@Path("/sync")
public class SyncController {

    /**
     * Get the full list of syncs currently stored in the database.
     * The results is paged so one page only contains 20 entities at a time.
     * @param page Page index you want to get. Defaults to 0 if the query parameter is not given.
     * @return Array containing 20 {@link Sync} objects from the given page from the database.
     */
    @GET
    @CheckAuth
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sync> getSyncs(@QueryParam("page") long page) {
        QueryBuilder<Sync, String> queryBuilder = H2Helper.getSyncDao().getQueryBuilder();
        try {
            return queryBuilder.orderBy("syncEnd", false).limit(20L).offset(page*20L).query();
        } catch (SQLException ex) {
            throw new GenericException(Response.Status.INTERNAL_SERVER_ERROR, "internal_error", ex.getMessage());
        }
    }

    /**
     * Get a single {@link Sync} by supplying it's id.
     * @param syncId Id of the {@link Sync} you want to get.
     * @return The {@link Sync} object that matches the given id.
     */
    @GET
    @CheckAuth
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sync getSync(@NotEmpty @PathParam("id") String syncId) {
        Sync sync = H2Helper.getSyncDao().getByAttributeMatch("id", syncId);
        if (sync == null) throw new NotFoundException("sync_not_existing");
        return sync;
    }

    /**
     * Get all users that were last modified by the given sync.
     * @param syncId Id of the sync the modified users should be got for.
     * @return List of users that were last changed by the given sync.
     */
    @GET
    @CheckAuth
    @Path("/{id}/users")
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getSyncUsers(@NotEmpty @PathParam("id") String syncId) {
        Sync sync = H2Helper.getSyncDao().getByAttributeMatch("id", syncId);
        if (sync == null) throw new NotFoundException("sync_not_existing");
        return H2Helper.getUserDao().getAllByAttributeMatch("lastSyncId", sync.getId());
    }

}
