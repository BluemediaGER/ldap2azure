package de.traber_info.home.ldap2azure.rest.controller;

import de.traber_info.home.ldap2azure.h2.H2Helper;
import de.traber_info.home.ldap2azure.rest.anotation.CheckAuth;
import de.traber_info.home.ldap2azure.rest.model.response.DashboardResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;

/**
 * REST controller to handle all actions that do not fit to one of the other controllers.
 *
 * @author Oliver Traber
 */
@Path("/")
public class MiscController {

    /**
     * Method to get the {@link DashboardResponse} object used by the frontend dashboard.
     * @return Returns the {@link DashboardResponse} for use in the frontend.
     */
    @GET
    @CheckAuth
    @Path("/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                H2Helper.getSyncDao().getRecent(4),
                H2Helper.getUserDao().getAmount(),
                H2Helper.getUserDao().getOkAmount(),
                H2Helper.getUserDao().getPendingAmount(),
                H2Helper.getUserDao().getFailedAmount()
        );
    }

    /**
     * Easter egg to have some fun while using the api.
     * @return Some funny content.
     */
    @GET
    @Path("/tea-time")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEasterEgg() {
        String[] types = {"Black Tea", "Green Tea", "Peppermint Tea", "Rosehip Tea", "Fruit Tea"};
        int rnd = new Random().nextInt(types.length);
        return Response.status(418).entity("{\"flavor\":\"" + types[rnd] + "\"}").build();
    }

}
