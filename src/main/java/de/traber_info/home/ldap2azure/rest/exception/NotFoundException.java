package de.traber_info.home.ldap2azure.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Oliver Traber
 *
 * ApplicationException thrown when an requested object is not existent.
 */
public class NotFoundException extends WebApplicationException {

    /**
     * Serial version for this class.
     */
    private static final long serialVersionUID = -3421690330574443604L;

    /**
     * ApplicationException thrown when an requested object is not existent.
     * @param errorMessage Error message that should be embedded in the error json response.
     */
    public NotFoundException(String errorMessage) {
        super(Response
        .status(Response.Status.NOT_FOUND)
        .entity("{\"error\":\"" + errorMessage + "\"}")
        .type(MediaType.APPLICATION_JSON)
        .build());
    }

}