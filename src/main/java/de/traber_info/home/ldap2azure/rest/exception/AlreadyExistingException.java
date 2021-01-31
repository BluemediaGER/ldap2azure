package de.traber_info.home.ldap2azure.rest.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Oliver Traber
 *
 * ApplicationException thrown when an object that should be created is already existent.
 */
public class AlreadyExistingException extends WebApplicationException {

    /**
     * Serial version for this class.
     */
    private static final long serialVersionUID = 8502083684301958191L;

    /**
     * ApplicationException thrown when an object that should be created is already existent.
     * @param errorMessage Error message that should be embedded in the error json response.
     */
    public AlreadyExistingException(String errorMessage) {
        super(Response
        .status(422)
        .entity("{\"error\":\"" + errorMessage + "\"}")
        .type(MediaType.APPLICATION_JSON)
        .build());
    }

}