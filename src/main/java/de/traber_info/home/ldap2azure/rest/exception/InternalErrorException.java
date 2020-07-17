package de.traber_info.home.ldap2azure.rest.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Oliver Traber
 *
 * ApplicationException thrown when any internal exception occur.
 */
public class InternalErrorException extends WebApplicationException {

    /**
     * Serial version for this class.
     */
    private static final long serialVersionUID = 8502083684312568191L;

    /**
     * ApplicationException thrown when any internal exception occur.
     * @param errorMessage Error message that should be embedded in the error json response.
     */
    public InternalErrorException(String errorMessage, long errorCode) {
        super(Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"" + errorMessage + "\", \"code\":" + errorCode + "}")
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

}
