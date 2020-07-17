package de.traber_info.home.ldap2azure.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Oliver Traber
 *
 * ApplicationException thrown when an client sends an invalid request.
 */
public class BadRequestException extends WebApplicationException {

    /**
     * Serial version for this class.
     */
    private static final long serialVersionUID = 4449670222669523644L;

    /**
     * ApplicationException thrown when an client sends an invalid request.
     * @param errorMessage Error message that should be embedded in the error json response.
     */
    public BadRequestException(String errorMessage) {
        super(Response
        .status(Response.Status.BAD_REQUEST)
        .entity("{\"error\":\"" + errorMessage + "\"}")
        .type(MediaType.APPLICATION_JSON)
        .build());
    }

}