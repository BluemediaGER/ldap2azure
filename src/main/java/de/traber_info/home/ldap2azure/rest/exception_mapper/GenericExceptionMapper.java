package de.traber_info.home.ldap2azure.rest.exception_mapper;

import de.traber_info.home.ldap2azure.rest.model.response.GenericError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutionException;

/**
 * {@link ExceptionMapper} implementation used to map any {@link Exception} to a response.
 *
 * @author Oliver Traber
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(GenericExceptionMapper.class.getName());

    /**
     * Map an given {@link Exception} to a response.
     * @param ex {@link Exception} that should be mapped.
     * @return {@link Response} containing the stacktrace.
     */
    @Override
    public Response toResponse(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        LOG.error("An unexpected error occurred", ex);
        GenericError error = new GenericError();
        error.error = "internal_error";
        error.message = sw.toString();
        return Response
                .status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }

}
