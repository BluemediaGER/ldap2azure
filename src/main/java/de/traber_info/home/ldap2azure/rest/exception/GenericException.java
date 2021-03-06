package de.traber_info.home.ldap2azure.rest.exception;

import de.traber_info.home.ldap2azure.rest.model.response.GenericError;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Generic Exception for cases where an human readable message should be included.
 */
public class GenericException extends WebApplicationException {

    /**
     * Serial version for this class.
     */
    private static final long serialVersionUID = 4449670323669523644L;

    /**
     * ApplicationException thrown when an client sends an invalid request.
     * @param error Machine readable error code.
     * @param message Error message containing further details for manual review.
     */
    public GenericException(Response.Status httpStatus, String error, String message) {
        super(Response
                .status(httpStatus)
                .entity(new GenericError(error, message))
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

}
