package de.traber_info.home.ldap2azure.rest.exception_mapper;

import de.traber_info.home.ldap2azure.rest.model.response.GenericError;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * {@link ExceptionMapper} implementation used to map an {@link NotFoundException} to a json response.
 */
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    /**
     * Map an given {@link NotFoundException} to a response.
     * @param ex {@link NotFoundException} that should be mapped.
     * @return {@link Response} containing the json error object.
     */
    @Override
    public Response toResponse(NotFoundException ex) {
        GenericError error = new GenericError();
        error.error = "not_found";
        error.message = "The given path could not be found by the server.";
        return Response
                .status(404)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }

}
