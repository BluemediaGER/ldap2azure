package de.traber_info.home.ldap2azure.rest.exception_mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.traber_info.home.ldap2azure.rest.model.response.GenericError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * @author Oliver Traber
 *
 * {@link ExceptionMapper} implementation used to map {@link JsonProcessingException} to a response.
 */
@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    /**
     * Map an given {@link Exception} to a response.
     * @param ex {@link JsonProcessingException} required to overwrite the toResponse method.
     * @return {@link Response} containing the stacktrace.
     */
    @Override
    public Response toResponse(JsonProcessingException ex) {
        GenericError error = new GenericError();
        error.error = "bad_json";
        error.message = "Your request contains invalid json. Probably you have a typo or a syntax violation.";
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

}
